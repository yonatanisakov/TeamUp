package com.idz.teamup.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idz.teamup.repository.AuthRepo
import kotlinx.coroutines.launch

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

    fun register(fullName: String, email: String, password: String) {
        viewModelScope.launch {
            val result = authRepository.register(fullName, email, password)
            _authResult.postValue(result)
        }
    }
}
