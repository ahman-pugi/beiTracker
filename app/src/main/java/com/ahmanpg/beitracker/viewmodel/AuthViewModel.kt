package com.ahmanpg.beitracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmanpg.beitracker.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _user = MutableStateFlow<FirebaseUser?>(authRepository.currentUser)
    val user: StateFlow<FirebaseUser?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.authStateFlow().collect {
                _user.value = it
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = authRepository.signIn(email, password)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message ?: "Login failed"
            }
            _isLoading.value = false
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = authRepository.signInWithGoogle(idToken)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message ?: "Google Sign-In failed"
            }
            _isLoading.value = false
        }
    }

    fun signUp(email: String, password: String, name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = authRepository.signUp(email, password, name)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message ?: "Signup failed"
            }
            _isLoading.value = false
        }
    }

    fun updateProfile(name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.updateProfile(name)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message ?: "Profile update failed"
            }
            _isLoading.value = false
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.sendPasswordResetEmail(email)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message ?: "Reset email failed"
            } else {
                _error.value = "Reset email sent!" // Using error field for feedback too
            }
            _isLoading.value = false
        }
    }

    fun signOut() {
        authRepository.signOut()
    }

    fun clearError() {
        _error.value = null
    }
}
