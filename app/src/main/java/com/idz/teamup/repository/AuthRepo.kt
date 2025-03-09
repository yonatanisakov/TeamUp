package com.idz.teamup.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.idz.teamup.model.User
import com.idz.teamup.service.StorageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepo {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storageService = StorageService.getInstance()

    suspend fun login(email: String, password: String): Pair<Boolean, String?> {
        return withContext(Dispatchers.IO) {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                Pair(true, null)
            } catch (e: Exception) {
                Log.e("TeamUp", "Error in ${this::class.java.simpleName}: ${e.message}", e)

                Pair(false, e.message)
            }
        }
    }

    suspend fun register(fullName: String, email: String, password: String, profileImageUri: Uri? = null): Pair<Boolean, String?> {
        return withContext(Dispatchers.IO) {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val userId = result.user?.uid ?: return@withContext Pair(false, "User ID is null")

                var profileImageUrl = ""

                if (profileImageUri != null) {
                    try {
                        profileImageUrl = storageService.uploadProfileImage(userId, profileImageUri)
                    } catch (e: Exception) {
                        Log.e("AuthRepo", "Error uploading profile image", e)
                    }
                }

                val newUser = User(userId, fullName, email, profileImageUrl)
                db.collection("users").document(userId).set(newUser).await()

                auth.signOut()

                Pair(true, null)
            } catch (e: Exception) {
                Log.e("TeamUp", "Error in ${this::class.java.simpleName}: ${e.message}", e)

                Pair(false, e.message)
            }
        }
    }
}
