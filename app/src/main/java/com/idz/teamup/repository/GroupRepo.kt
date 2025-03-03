package com.idz.teamup.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.idz.teamup.model.Group
import kotlinx.coroutines.tasks.await

class GroupRepo {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun createGroup(group: Group): Boolean {
        return try {
            val groupId = db.collection("groups").document().id
            val newGroup = group.copy(groupId = groupId, createdBy = auth.currentUser?.email ?: "")
            db.collection("groups").document(groupId).set(newGroup).await()
            true
        } catch (e: Exception) {
            false
        }
    }
    suspend fun getGroups(): List<Group> {
        return try {
            val snapshot = db.collection("groups").get().await()
            snapshot.documents.mapNotNull { it.toObject(Group::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
