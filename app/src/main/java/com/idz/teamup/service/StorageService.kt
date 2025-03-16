package com.idz.teamup.service

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class StorageService {
    private val storage = FirebaseStorage.getInstance()

    companion object {
        private var instance: StorageService? = null

        fun getInstance(): StorageService {
            if (instance == null) {
                instance = StorageService()
            }
            return instance as StorageService
        }
    }

    suspend fun uploadProfileImage(userId: String, imageUri: Uri): String {
        return withContext(Dispatchers.IO) {
            val imageRef = storage.reference.child("profile_images/$userId.jpg")
            imageRef.putFile(imageUri).await()
            imageRef.downloadUrl.await().toString()
        }
    }
}