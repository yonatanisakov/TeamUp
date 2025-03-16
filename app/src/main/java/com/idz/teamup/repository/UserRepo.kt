package com.idz.teamup.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.idz.teamup.model.User
import com.idz.teamup.service.StorageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserRepo {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storageService = StorageService.getInstance()

    suspend fun getCurrentUser(): User? {
        return withContext(Dispatchers.IO) {
            val userId = auth.currentUser?.uid ?: return@withContext null
            try {
                val document = db.collection("users").document(userId).get().await()
                document.toObject(User::class.java)
            } catch (e: Exception) {
                Log.e("TeamUp", "Error in ${this::class.java.simpleName}: ${e.message}", e)
                null
            }
        }
    }

    suspend fun updateUserProfile(user: User): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                db.collection("users").document(user.userId).set(user).await()
                true
            } catch (e: Exception) {
                Log.e("TeamUp", "Error in ${this::class.java.simpleName}: ${e.message}", e)
                false
            }
        }
    }

    suspend fun uploadProfilePicture(imageUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            val userId = auth.currentUser?.uid ?: return@withContext null
            try {
                storageService.uploadProfileImage(userId, imageUri)
            } catch (e: Exception) {
                Log.e("UserRepo", "Error uploading profile picture: ${e.message}", e)
                null
            }
        }
    }

    suspend fun deleteProfilePicture(): Boolean {
        return withContext(Dispatchers.IO) {
            val userId = auth.currentUser?.uid ?: return@withContext false
            try {
                val imageRef = FirebaseStorage.getInstance().reference.child("profile_images/$userId.jpg")
                imageRef.delete().await()
                true
            } catch (e: Exception) {
                Log.e("UserRepo", "Error deleting profile picture: ${e.message}", e)
                false
            }
        }
    }

    fun logout() {
        auth.signOut()
    }
}