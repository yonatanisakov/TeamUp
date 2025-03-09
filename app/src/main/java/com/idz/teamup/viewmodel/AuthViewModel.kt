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

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            _authResult.postValue(result)
        }
    }

    fun register(fullName: String, email: String, password: String, profileImageUri: Uri? = null) {
        viewModelScope.launch {
            val result = authRepository.register(fullName, email, password, profileImageUri)
            withContext(Dispatchers.Main) {
                _authResult.postValue(result)
            }
        }
    }
}
