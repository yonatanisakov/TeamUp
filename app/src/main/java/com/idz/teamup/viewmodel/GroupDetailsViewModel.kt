package com.idz.teamup.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope

import com.idz.teamup.model.Group
import com.idz.teamup.repository.GroupRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

class GroupDetailsViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = GroupRepo(application)

    private var _group = MutableLiveData<Group?>()
    val group: LiveData<Group?> get() = _group


    private val _members = MutableLiveData<List<String>>()
    val members: LiveData<List<String>> get() = _members

    private val _weather = MutableLiveData<String>()
    val weather: LiveData<String> get() = _weather

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun loadGroupDetails(groupId: String) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                supervisorScope {
                    repo.getGroupDetails(groupId).observeForever { entity ->
                        entity?.let {
                            _group.postValue(it.toGroup())
                            _members.postValue(it.members)
                            _isLoading.postValue(false)
                        }
                    }

                    try {
                        repo.fetchGroupDetailsFromFirestore(groupId)
                    } catch (e: Exception) {
                        if (e is CancellationException) {
                            Log.d("GroupDetailsViewModel", "Fetch cancelled due to lifecycle change")
                        } else {
                            Log.e("GroupDetailsViewModel", "Error loading group details: ${e.message}", e)
                        }
                    }
                }
            } catch (e: Exception) {
                _isLoading.postValue(false)
                if (e is CancellationException) {
                    Log.d("GroupDetailsViewModel", "Operation cancelled due to lifecycle change")
                } else {
                    Log.e("GroupDetailsViewModel", "Error loading group details: ${e.message}", e)
                }
            }
        }
    }


        fun isUserMember(): Boolean {
            return group.value?.members?.contains(repo.auth.currentUser?.email) == true
        }

        fun isUserCreator(): Boolean {
            return group.value?.createdBy == repo.auth.currentUser?.email
        }


    fun updateGroupDetails(
        groupId: String,
        newName: String,
        newDesc: String,
        newImageUri: Uri?,
        onComplete: (Boolean) -> Unit
    ) {
        _isLoading.value = true

        viewModelScope.launch {
            withContext(NonCancellable) {
                try {
                    var success = false
                    try {
                        repo.updateGroupDetails(groupId, newName, newDesc, newImageUri) { result ->
                            success = result
                            if (success) {
                                GroupViewModel.updatedGroupId = groupId
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("GroupDetailsViewModel", "Error updating group details: ${e.message}", e)
                        success = false
                    }

                    withContext(Dispatchers.Main) {
                        try {
                            _isLoading.value = false
                            onComplete(success)
                        } catch (e: Exception) {
                            Log.d("GroupDetailsViewModel", "Could not deliver callback - fragment likely detached")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("GroupDetailsViewModel", "Unexpected error in update coroutine: ${e.message}", e)
                }
            }
        }
    }



    fun deleteGroup(groupId: String, onComplete: (Boolean) -> Unit) {
        _isLoading.value = true

        viewModelScope.launch {
            withContext(NonCancellable) {
                try {
                    var success = false
                    try {
                        repo.deleteGroup(groupId) { result ->
                            success = result
                            if (success) {
                                GroupViewModel.refreshGroups = true
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("GroupDetailsViewModel", "Error during group deletion: ${e.message}", e)
                        success = false
                    }

                    withContext(Dispatchers.Main) {
                        try {
                            _isLoading.value = false
                            onComplete(success)
                        } catch (e: Exception) {
                            Log.d("GroupDetailsViewModel", "Could not deliver callback - fragment likely detached")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("GroupDetailsViewModel", "Unexpected error in deletion coroutine: ${e.message}", e)
                }
            }
        }
    }


    fun toggleGroupMembership(onComplete: (Boolean) -> Unit) {
        _isLoading.value = true
        val groupId = _group.value?.groupId ?: return
        val group = _group.value ?: return

        // Special handling for trying to join a full group
        if (!isUserMember() &&
            group.maxParticipants > 0 &&
            group.members.size >= group.maxParticipants) {
            _isLoading.postValue(false)
            onComplete(false)
            return
        }

        viewModelScope.launch {
            try {
                repo.toggleGroupMembership(groupId) { success ->
                    _isLoading.postValue(false)
                    if (success) {
                        loadGroupDetails(groupId)
                    }
                    onComplete(success)
                }
            }catch (e: Exception) {
                _isLoading.postValue(false)
                onComplete(false)
                Log.e("GroupDetailsViewModel", "Error updating membership: ${e.message}", e)
            }
        }
    }

    fun loadWeather(city: String, dateTime: String) {
        viewModelScope.launch {
            val localWeather = group.value?.weather
            if (!localWeather.isNullOrEmpty()) {
                _weather.postValue(localWeather)
            } else {
                WeatherRepo.fetchWeather(city, dateTime, "HJBa6wYcxMgPDR4kOlmaIgzDVAOMbaor") { result ->
                    _weather.postValue(result)
                    viewModelScope.launch(Dispatchers.IO) {
                        repo.updateWeather(group.value?.groupId ?: return@launch, result)
                    }
                }
            }
        }
    }

}

