package com.idz.teamup.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.idz.teamup.local.GroupDatabase
import com.idz.teamup.local.entity.GroupEntity
import com.idz.teamup.model.Group
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

class GroupRepo(context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    val auth = FirebaseAuth.getInstance()
    private val groupDao = GroupDatabase.getDatabase(context).groupDao()
    private var loadGroupsJob: kotlinx.coroutines.Job? = null

    suspend fun createGroup(group: Group): Pair<Boolean, String?> {
        return try {
            val groupId = db.collection("groups").document().id
            val newGroup = group.copy(groupId = groupId, createdBy = auth.currentUser?.email ?: "")
            db.collection("groups").document(groupId).set(newGroup).await()
            Pair(true, groupId)

        } catch (e: Exception) {
            Log.e("TeamUp", "Error in ${this::class.java.simpleName}: ${e.message}", e)
            Pair(false, null)
        }
    }
    suspend fun getGroups() {
        loadGroupsJob?.cancel()

        loadGroupsJob = coroutineScope {
            launch(Dispatchers.IO) {

                try {
                    val cachedGroups = groupDao.getAllGroupsSync()

                    val snapshot = db.collection("groups").get().await()
                    val groups = snapshot.toObjects(Group::class.java)
                    val groupEntities = groups.map { it.toGroupEntity() }

                    if (groupEntities.isNotEmpty()) {
                        groupDao.clearGroups()
                        groupDao.insertGroups(groupEntities)
                    } else if (cachedGroups.isEmpty()) {
                        Log.w("GroupRepo", "Firestore returned empty list, and no cached data available.")
                    }
                } catch (e: Exception) {
                    Log.e("GroupRepo", "Error fetching Firestore. Keeping cached Room data.", e)
                }
            }
        }
        loadGroupsJob?.join()
    }

    fun getGroupDetails(groupId: String): LiveData<GroupEntity?> {
        return groupDao.getGroupById(groupId)
    }

    suspend fun fetchGroupDetailsFromFirestore(groupId: String) {
        try {
            withContext(Dispatchers.IO) {
                val snapshot = db.collection("groups").document(groupId).get().await()
                val group = snapshot.toObject(Group::class.java)
                if (group != null) {
                    groupDao.insertGroup(group.toGroupEntity())
                } else {
                    Log.e("GroupRepo", "Firestore group not found!")
                }
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) {
                Log.d("GroupRepo", "Firestore fetch cancelled due to navigation/lifecycle change")
            } else {
                Log.e("GroupRepo", "Error fetching group from Firestore", e)
            }
        }
    }

    suspend fun toggleGroupMembership(groupId: String, onComplete: (Boolean) -> Unit) {
        val userEmail = auth.currentUser?.email ?: return
        val groupRef = db.collection("groups").document(groupId)

        withContext(Dispatchers.IO) {
            try {
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(groupRef)
                    val group = snapshot.toObject(Group::class.java) ?: return@runTransaction

                    val updatedMembers = group.members.toMutableList()
                    if (updatedMembers.contains(userEmail)) {
                        updatedMembers.remove(userEmail)
                    } else {
                        updatedMembers.add(userEmail)
                    }

                    transaction.update(groupRef, "members", updatedMembers)
                    updatedMembers
                }.await()

                fetchGroupDetailsFromFirestore(groupId)
                withContext(Dispatchers.Main) {
                    onComplete(true)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("GroupRepo", "Error updating membership: ${e.message}", e)
                    onComplete(false)
                }
            }
        }
    }
    suspend fun uploadImageToFirebase(imageUri: Uri): String {
        return withContext(Dispatchers.IO) {
            try {
                val randomFileName = "group_images/${UUID.randomUUID()}.jpg"
                val storageRef = storage.reference.child(randomFileName)
                storageRef.putFile(imageUri).await()
                val downloadUrl = storageRef.downloadUrl.await()
                downloadUrl.toString()
            } catch (e: Exception) {
                Log.e("GroupRepo", "Error uploading image: ${e.message}", e)
                throw e
            }
        }
    }
    suspend fun updateGroupDetails(
        groupId: String,
        newName: String,
        newDesc: String,
        newImageUri: Uri?,
        onComplete: (Boolean) -> Unit
    ) {
        val updates = mutableMapOf<String, Any>(
            "name" to newName,
            "description" to newDesc
        )

        withContext(Dispatchers.IO) {
            try {
                if (newImageUri != null) {
                    try {
                        val imageUrl = uploadImageToFirebase(newImageUri)
                        updates["imageUrl"] = imageUrl
                    } catch (e: Exception) {
                        Log.e("GroupRepo", "Failed to upload image, continuing with other updates", e)
                    }
                }

                db.collection("groups").document(groupId).update(updates).await()

                val localGroup = groupDao.getGroupByIdSync(groupId)
                localGroup?.let {
                    val updatedEntity = it.copy(
                        name = newName,
                        description = newDesc,
                        imageUrl = (newImageUri ?: it.imageUrl).toString()
                    )
                    groupDao.insertGroup(updatedEntity)
                }

                withContext(Dispatchers.Main) {
                    onComplete(true)
                }
            } catch (e: Exception) {
                Log.e("TeamUp", "Error in ${this::class.java.simpleName}: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onComplete(false)
                }
            }
        }
    }
    suspend fun deleteGroup(groupId: String, onComplete: (Boolean) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                db.collection("groups").document(groupId).delete().await()
                try {
                    val group = groupDao.getGroupByIdSync(groupId)
                    if (group != null) {
                        groupDao.deleteGroup(groupId)
                    }
                } catch (e: Exception) {
                    Log.e("GroupRepo", "Error deleting from local database: ${e.message}", e)
                }
                getGroups()

                withContext(Dispatchers.Main) {
                    onComplete(true)
                }
            } catch (e: Exception) {
                Log.e("TeamUp", "Error in ${this::class.java.simpleName}: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onComplete(false)
                }
            }
        }
    }
    fun getAllGroupsFromRoom(): LiveData<List<GroupEntity>> {
        return groupDao.getAllGroupsLive()
    }
    suspend fun updateWeather(groupId: String, weather: String) {
        withContext(Dispatchers.IO) {
            db.collection("groups").document(groupId).update("weather", weather).await()
            groupDao.updateWeather(groupId, weather)
        }
    }
    suspend fun getAllGroupsFromRoomSync(): List<GroupEntity> {
        return withContext(Dispatchers.IO) {
            groupDao.getAllGroupsSync()
        }
    }

}
