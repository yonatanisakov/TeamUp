package com.idz.teamup.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope

import com.idz.teamup.model.Group
import com.idz.teamup.repository.GroupRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GroupDetailsViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = GroupRepo(application)

    private var _group = MutableLiveData<Group?>()
    val group: LiveData<Group?> get() = _group


    private val _members = MutableLiveData<List<String>>()
    val members: LiveData<List<String>> get() = _members

    private val _weather = MutableLiveData<String>()
    val weather: LiveData<String> get() = _weather

    fun loadGroupDetails(groupId: String) {
        viewModelScope.launch() {
            repo.getGroupDetails(groupId).observeForever { entity ->
                entity?.let {
                    _group.postValue(it.toGroup())
                    _members.postValue(it.members)
                }
            }
            repo.fetchGroupDetailsFromFirestore(groupId)
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
        viewModelScope.launch {
            repo.updateGroupDetails(groupId, newName, newDesc, newImageUri){ success ->
                if (success) {
                    loadGroupDetails(groupId)
                    GroupViewModel.updatedGroupId = groupId
                }
                onComplete(success)
            }
        }
    }


        fun deleteGroup(groupId: String, onComplete: (Boolean) -> Unit) {
            viewModelScope.launch {
                repo.deleteGroup(groupId){
                        success ->
                    if (success)
                        GroupViewModel.refreshGroups = true
                    onComplete(success)
                }
            }
        }


    fun toggleGroupMembership(onComplete: (Boolean) -> Unit) {
        val groupId = _group.value?.groupId ?: return
        viewModelScope.launch {
            repo.toggleGroupMembership(groupId) { success ->
                if (success) {
                    loadGroupDetails(groupId)
                }
                onComplete(success)
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

