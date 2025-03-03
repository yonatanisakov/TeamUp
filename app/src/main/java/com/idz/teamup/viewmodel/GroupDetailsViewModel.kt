package com.idz.teamup.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.idz.teamup.model.Group
import kotlinx.coroutines.launch
import java.util.UUID

class GroupDetailsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val _weather = MutableLiveData<String>()
    val weather: LiveData<String> get() = _weather

    private val _group = MutableLiveData<Group?>()
    val group: LiveData<Group?> get() = _group
    private val _members = MutableLiveData<List<String>>()
    val members: LiveData<List<String>> get() = _members
    fun loadGroupDetails(groupId: String) {
        db.collection("groups").document(groupId)
            .addSnapshotListener { document, _ ->
                if (document != null && document.exists()) {
                    val group = document.toObject(Group::class.java)
                    if (group != null) {
                        _group.postValue(group)
                        loadGroupMembers(group.members)
                    }
                }
            }
    }

    fun loadGroupMembers(memberEmails: List<String>) {
        viewModelScope.launch {
            _members.postValue(memberEmails)
        }
    }



    fun isUserMember(): Boolean {
        val userEmail = auth.currentUser?.email ?: return false
        return _group.value?.members?.contains(userEmail) == true
    }

    fun updateGroupDetails(groupId: String, newName: String, newDesc: String, newImageUri: Uri?, onComplete: (Boolean) -> Unit) {
        val groupRef = db.collection("groups").document(groupId)

        if (newImageUri != null) {
            val imageId = UUID.randomUUID().toString()

            val imageRef = FirebaseStorage.getInstance().reference.child("group_images/$imageId.jpg")
            imageRef.putFile(newImageUri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val updates = mapOf(
                            "name" to newName,
                            "description" to newDesc,
                            "imageUrl" to uri.toString()
                        )
                        groupRef.update(updates)
                            .addOnSuccessListener { onComplete(true) }
                            .addOnFailureListener { onComplete(false) }
                    }
                }
                .addOnFailureListener {
                    onComplete(false)
                }
        } else {
            val updates = mapOf(
                "name" to newName,
                "description" to newDesc
            )
            groupRef.update(updates)
                .addOnSuccessListener { onComplete(true) }
                .addOnFailureListener { onComplete(false) }
        }
    }


    fun deleteGroup(groupId: String, onComplete: (Boolean) -> Unit) {
        db.collection("groups").document(groupId)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
    fun isUserCreator(): Boolean {
        val userId = auth.currentUser?.email ?: return false
        return _group.value?.createdBy == userId
    }


    fun toggleGroupMembership(onComplete: (Boolean) -> Unit) {
        val userEmail = auth.currentUser?.email ?: return
        val groupId = _group.value?.groupId ?: return
        val groupRef = db.collection("groups").document(groupId)

        viewModelScope.launch {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(groupRef)
                val currentGroup = snapshot.toObject(Group::class.java) ?: return@runTransaction emptyList<String>()

                val updatedMembers = currentGroup.members.toMutableList()
                if (updatedMembers.contains(userEmail)) {
                    updatedMembers.remove(userEmail)
                } else {
                    updatedMembers.add(userEmail)
                }

                transaction.update(groupRef, "members", updatedMembers)
                return@runTransaction updatedMembers
            }.addOnSuccessListener { updatedMembers ->
                _group.value = _group.value?.copy(members = updatedMembers)
                onComplete(true)
            }.addOnFailureListener {
                onComplete(false)
            }
        }
    }

    fun loadWeather(city: String, dateTime: String) {

        WeatherRepo.fetchWeather(city, dateTime, "HJBa6wYcxMgPDR4kOlmaIgzDVAOMbaor") { result ->
            _weather.postValue(result)

        }
    }
}
