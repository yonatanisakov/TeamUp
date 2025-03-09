package com.idz.teamup.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.idz.teamup.model.User
import com.idz.teamup.repository.UserRepo
import com.idz.teamup.service.StorageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProfileViewModel : ViewModel() {
    private val userRepository = UserRepo()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storageService = StorageService.getInstance()

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
            withContext(Dispatchers.Main) {
                onComplete(success)
            }
        }
    }

    fun uploadProfilePicture(imageUri: Uri, onComplete: (String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                withContext(Dispatchers.Main) {
                    onComplete(null)
                }
                return@launch
            }

            try {
                val downloadUrl = storageService.uploadProfileImage(userId, imageUri)
                withContext(Dispatchers.Main) {
                    onComplete(downloadUrl)
                }
            } catch (e: Exception) {
                Log.e("TeamUp", "Error in ${this::class.java.simpleName}: ${e.message}", e)

                withContext(Dispatchers.Main) {
                    onComplete(null)
                }
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
                        withContext(Dispatchers.Main) {
                            onComplete(success)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            onComplete(false)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("TeamUp", "Error in ${this::class.java.simpleName}: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        onComplete(false)
                    }
                }
            }
        }

        fun logout() {
            auth.signOut()
        }
    }
