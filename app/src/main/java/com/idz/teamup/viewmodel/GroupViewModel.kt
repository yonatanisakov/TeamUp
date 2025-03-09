package com.idz.teamup.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.idz.teamup.local.entity.GroupEntity
import com.idz.teamup.model.Group
import com.idz.teamup.repository.GroupRepo
import kotlinx.coroutines.launch

class GroupViewModel(application: Application) : AndroidViewModel(application) {

    private val groupRepo = GroupRepo(application)
    val groups: LiveData<List<GroupEntity>> = groupRepo.getAllGroupsFromRoom()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading
    companion object {
        var refreshGroups = false
        var updatedGroupId: String? = null
    }
    fun createGroup(group: Group, onComplete: (Boolean) -> Unit) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                if (group.imageUrl.isNotEmpty()) {
                    val imageUri = Uri.parse(group.imageUrl)
                    val uploadedUrl = groupRepo.uploadImageToFirebase(imageUri)

                    val updatedGroup = group.copy(imageUrl = uploadedUrl)
                    val success = groupRepo.createGroup(updatedGroup)

                    if (success) {
                        refreshGroups = true
                    }
                    onComplete(success)
                } else {
                    val success = groupRepo.createGroup(group)
                    if (success) {
                        refreshGroups = true
                    }
                    onComplete(success)
                }
            } catch (e: Exception) {
                Log.e("TeamUp", "Error in ${this::class.java.simpleName}: ${e.message}", e)

                onComplete(false)

            } finally {
                _isLoading.value = false
            }
        }
    }


    fun loadGroups(forceRefresh: Boolean = false) {
        if (!forceRefresh && groups.value != null && groups.value?.isNotEmpty() == true && updatedGroupId == null) {
            return
        }

        _isLoading.value = true


        val specificGroupUpdate = updatedGroupId
        updatedGroupId = null
        refreshGroups = false

        viewModelScope.launch {
            if (specificGroupUpdate != null) {
                groupRepo.fetchGroupDetailsFromFirestore(specificGroupUpdate)
            } else {
                groupRepo.getGroups()
            }
            _isLoading.value = false

        }
    }


}
