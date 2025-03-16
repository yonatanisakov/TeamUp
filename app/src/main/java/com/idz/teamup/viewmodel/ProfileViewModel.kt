package com.idz.teamup.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idz.teamup.model.User
import com.idz.teamup.repository.UserRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileViewModel : ViewModel() {
    private val userRepository = UserRepo()

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            val fetchedUser = userRepository.getCurrentUser()
            _user.postValue(fetchedUser)
            _isLoading.value = false
        }
    }

    fun updateUserWithImage(updatedUser: User, imageUri: Uri, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val imageUrl = userRepository.uploadProfilePicture(imageUri)
                if (imageUrl != null) {
                    val userWithImage = updatedUser.copy(profileImageUrl = imageUrl)
                    val success = userRepository.updateUserProfile(userWithImage)
                    if (success) _user.value = userWithImage
                    withContext(Dispatchers.Main) { onComplete(success) }
                } else {
                    withContext(Dispatchers.Main) { onComplete(false) }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUserWithoutImage(updatedUser: User, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = userRepository.updateUserProfile(updatedUser)
                if (success) _user.value = updatedUser
                withContext(Dispatchers.Main) { onComplete(success) }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteProfilePictureAndUpdate(updatedUser: User, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val deleteSuccess = userRepository.deleteProfilePicture()
                if (deleteSuccess) {
                    val userWithoutImage = updatedUser.copy(profileImageUrl = "")
                    val updateSuccess = userRepository.updateUserProfile(userWithoutImage)
                    if (updateSuccess) _user.value = userWithoutImage
                    withContext(Dispatchers.Main) { onComplete(updateSuccess) }
                } else {
                    withContext(Dispatchers.Main) { onComplete(false) }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        userRepository.logout()
    }
}