package com.team21.myapplication.ui.updateprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.data.model.StudentUser
import com.team21.myapplication.data.repository.StudentUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdateProfileViewModel(private val studentUserRepository: StudentUserRepository) : ViewModel() {

    private val _user = MutableStateFlow<StudentUser?>(null)
    val user: StateFlow<StudentUser?> = _user.asStateFlow()

    init {
        viewModelScope.launch {
            // Corrutina usando dispatcher: carga de usuario en IO
            withContext(Dispatchers.IO) {
                studentUserRepository.getCurrentUser().collect {
                    // Cambiar al Main para actualizar estado
                    withContext(Dispatchers.Main) {
                        _user.value = it
                    }
                }
            }
        }
    }

    fun updateUser(user: StudentUser, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            // Validación ligera en Main, persistencia en IO
            val result = withContext(Dispatchers.IO) {
                studentUserRepository.updateUser(user)
            }
            onResult(result)
        }
    }
}

class UpdateProfileViewModelFactory(private val studentUserRepository: StudentUserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UpdateProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UpdateProfileViewModel(studentUserRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
