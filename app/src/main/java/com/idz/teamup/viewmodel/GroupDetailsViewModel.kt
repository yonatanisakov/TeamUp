package com.idz.teamup.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.idz.teamup.model.Group
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GroupDetailsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _group = MutableLiveData<Group?>()
    val group: LiveData<Group?> get() = _group

    fun loadGroupDetails(groupId: String) {
        viewModelScope.launch {
            val document = db.collection("groups").document(groupId).get().await()
            _group.postValue(document.toObject(Group::class.java))
        }
    }

    fun isUserMember(): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        return _group.value?.members?.contains(userId) == true
    }

    fun toggleGroupMembership(onComplete: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val groupRef = db.collection("groups").document(_group.value?.groupId ?: return)

        viewModelScope.launch {
            val currentGroup = _group.value ?: return@launch
            val updatedMembers = currentGroup.members.toMutableList()

            if (isUserMember()) {
                updatedMembers.remove(userId)
            } else {
                updatedMembers.add(userId)
            }

            groupRef.update("members", updatedMembers).await()
            _group.postValue(currentGroup.copy(members = updatedMembers))
            onComplete(true)
        }
    }
}
