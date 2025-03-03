package com.idz.teamup.viewmodel

import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import com.idz.teamup.model.Group
import com.idz.teamup.repository.GroupRepo
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class GroupViewModel : ViewModel() {
    private val groupRepo = GroupRepo()
    private val _groups = MutableLiveData<List<Group>>()
    val groups: LiveData<List<Group>> get() = _groups
    fun createGroup(group: Group, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (group.imageUrl.isNotEmpty()) {
                try {
                    val imageId = UUID.randomUUID().toString()
                    val storageRef = FirebaseStorage.getInstance().reference.child("group_images/$imageId.jpg")
                    storageRef.putFile(group.imageUrl.toUri()).await()
                    group.imageUrl = storageRef.downloadUrl.await().toString()
                } catch (e: Exception) {
                    group.imageUrl = ""
                }
            }
            val success = groupRepo.createGroup(group)
            onComplete(success)
        }
    }


    fun loadGroups() {
        viewModelScope.launch {
            val groupList = groupRepo.getGroups()
            _groups.postValue(groupList)
        }
    }
    fun searchGroups(query: String) {
        val filteredGroups = _groups.value?.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.activityType.contains(query, ignoreCase = true)
        }
        _groups.postValue(filteredGroups ?: emptyList())
    }

}
