package com.idz.teamup.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.idz.teamup.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserRepo {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun getCurrentUser(): User? {
        return withContext(Dispatchers.IO) {
            val userId = auth.currentUser?.uid ?: return@withContext null
            try {
                val document = db.collection("users").document(userId).get().await()
                document.toObject(User::class.java)
            } catch (e: Exception) {
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
                false
            }
        }
    }
}
