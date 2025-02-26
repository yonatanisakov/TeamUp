package com.idz.teamup.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.idz.teamup.model.User
import com.idz.teamup.repository.UserRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {
    private val userRepository = UserRepo()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    fun loadUserProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            val fetchedUser = userRepository.getCurrentUser()
            _user.postValue(fetchedUser)
        }
    }

    fun updateUserProfile(user: User, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = userRepository.updateUserProfile(user)
            if (success) _user.postValue(user)
            onComplete(success)
        }
    }

    fun uploadProfilePicture(imageUri: Uri, onComplete: (String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                onComplete(null)
                return@launch
            }

            val imageRef = storage.reference.child("profile_images/$userId.jpg")
            try {
                imageRef.putFile(imageUri).await()
                val uri = imageRef.downloadUrl.await()
                onComplete(uri.toString()) // Return image URL instead of saving immediately
            } catch (e: Exception) {
                onComplete(null)
            }
        }
    }

    fun deleteProfilePicture(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val userId = auth.currentUser?.uid ?: return@launch
            val imageRef = storage.reference.child("profile_images/$userId.jpg")

            try {
                imageRef.delete().await()
                val updatedUser = _user.value?.copy(profileImageUrl = "")
                if (updatedUser != null) {
                    val success = userRepository.updateUserProfile(updatedUser)
                    _user.postValue(updatedUser)
                    onComplete(success)
                } else {
                    onComplete(false)
                }
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    fun logout() {
        auth.signOut()
    }
}
