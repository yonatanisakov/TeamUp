package com.idz.teamup.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idz.teamup.repository.AuthRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepo()

    private val _authResult = MutableLiveData<Pair<Boolean, String?>>()
    val authResult: LiveData<Pair<Boolean, String?>> get() = _authResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun login(email: String, password: String) {
        _isLoading.value = true

        viewModelScope.launch {
            val result = authRepository.login(email, password)
            _authResult.postValue(result)
            _isLoading.postValue(false)
        }
    }

    fun register(fullName: String, email: String, password: String, profileImageUri: Uri? = null) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = authRepository.register(fullName, email, password, profileImageUri)
            withContext(Dispatchers.Main) {
                _authResult.postValue(result)
                _isLoading.postValue(false)

            }
        }
    }
}
