package com.idz.teamup.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.idz.teamup.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepo {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun login(email: String, password: String): Pair<Boolean, String?> {
        return withContext(Dispatchers.IO) {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                Pair(true, null)
            } catch (e: Exception) {
                Pair(false, e.message)
            }
        }
    }

    suspend fun register(fullName: String, email: String, password: String): Pair<Boolean, String?> {
        return withContext(Dispatchers.IO) {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val userId = result.user?.uid ?: return@withContext Pair(false, "User ID is null")

                val newUser = User(userId, fullName, email, "")
                db.collection("users").document(userId).set(newUser).await()

                Pair(true, null)
            } catch (e: Exception) {
                Pair(false, e.message)
            }
        }
    }
}
