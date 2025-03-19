package com.idz.teamup.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.idz.teamup.local.entity.GroupEntity
import com.idz.teamup.repository.GroupRepo
import kotlinx.coroutines.launch

class MyGroupsViewModel(application: Application) : AndroidViewModel(application) {

    private val groupRepo = GroupRepo(application)
    private val auth = FirebaseAuth.getInstance()

    private val _myGroups = MutableLiveData<List<GroupEntity>>()
    val myGroups: LiveData<List<GroupEntity>> get() = _myGroups

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading


    private var isRefreshing = false


    init {
        loadMyGroups(forceRefresh = true)
    }

    fun loadMyGroups(forceRefresh: Boolean = false) {
        val currentUserEmail = auth.currentUser?.email ?: return

        if (isRefreshing) return

        isRefreshing = true
        _isLoading.value = true

        viewModelScope.launch {
            try {
                if (forceRefresh) {
                    groupRepo.getGroups()
                }
                val allGroups = groupRepo.getAllGroupsFromRoomSync()
                val myGroups = allGroups.filter { it.createdBy == currentUserEmail }
                _myGroups.postValue(myGroups)
            }
            catch (e: Exception) {
                Log.e("MyGroupsViewModel", "Error loading groups: ${e.message}", e)
            } finally {
                _isLoading.postValue(false)
                isRefreshing = false
            }
        }
    }

    fun deleteGroup(groupId: String, onComplete: (Boolean) -> Unit) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                groupRepo.deleteGroup(groupId) { success ->
                    if (success) {

                        val currentGroups = _myGroups.value ?: emptyList()
                        val updatedGroups = currentGroups.filter { it.groupId != groupId }
                        _myGroups.postValue(updatedGroups)
                    }

                    _isLoading.postValue(false)
                    onComplete(success)
                }
            }catch (e: Exception) {
                Log.e("MyGroupsViewModel", "Error deleting group: ${e.message}", e)
            }
        }
    }
}