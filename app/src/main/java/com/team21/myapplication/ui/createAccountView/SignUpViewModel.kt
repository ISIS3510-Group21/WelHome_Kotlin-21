package com.team21.myapplication.ui.createAccountView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SignUpViewModel : ViewModel() {
    private val repository = AuthRepository()

    // Forms States
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    private val _phonePrefix = MutableStateFlow("+57")
    val phonePrefix: StateFlow<String> = _phonePrefix.asStateFlow()

    private val _birthDay = MutableStateFlow("")
    val birthDay: StateFlow<String> = _birthDay.asStateFlow()

    private val _birthMonth = MutableStateFlow("")
    val birthMonth: StateFlow<String> = _birthMonth.asStateFlow()

    private val _birthYear = MutableStateFlow("")
    val birthYear: StateFlow<String> = _birthYear.asStateFlow()

    private val _gender = MutableStateFlow("")
    val gender: StateFlow<String> = _gender.asStateFlow()

    private val _nationality = MutableStateFlow("")
    val nationality: StateFlow<String> = _nationality.asStateFlow()

    private val _language = MutableStateFlow("")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _isStudent = MutableStateFlow(false)
    val isStudent: StateFlow<Boolean> = _isStudent.asStateFlow()

    private val _isHost = MutableStateFlow(false)
    val isHost: StateFlow<Boolean> = _isHost.asStateFlow()

    // Register state
    private val _signUpState = MutableStateFlow<SignUpState>(SignUpState.Idle)
    val signUpState: StateFlow<SignUpState> = _signUpState.asStateFlow()

    // Update functions
    fun updateName(value: String) { _name.value = value }
    fun updateEmail(value: String) { _email.value = value }
    fun updatePassword(value: String) { _password.value = value }
    fun updatePhoneNumber(value: String) { _phoneNumber.value = value }
    fun updatePhonePrefix(value: String) { _phonePrefix.value = value }
    fun updateBirthDay(value: String) { _birthDay.value = value }
    fun updateBirthMonth(value: String) { _birthMonth.value = value }
    fun updateBirthYear(value: String) { _birthYear.value = value }
    fun updateGender(value: String) { _gender.value = value }
    fun updateNationality(value: String) { _nationality.value = value }
    fun updateLanguage(value: String) { _language.value = value }

    fun toggleUserType(isStudentSelected: Boolean) {
        if (isStudentSelected) {
            _isStudent.value = true
            _isHost.value = false
        } else {
            _isStudent.value = false
            _isHost.value = true
        }
    }

    fun signUp() {
        // Validations
        if (_name.value.isBlank()) {
            _signUpState.value = SignUpState.Error("Name is mandatory")
            return
        }

        if (_email.value.isBlank() || !_email.value.contains("@")) {
            _signUpState.value = SignUpState.Error("Enter a valid email address")
            return
        }

        if (_password.value.length < 6) {
            _signUpState.value = SignUpState.Error("The password must be at least 6 characters long")
            return
        }

        if (_phoneNumber.value.isBlank()) {
            _signUpState.value = SignUpState.Error("The phone number is required")
            return
        }

        if (_birthDay.value.isBlank() || _birthMonth.value.isBlank() || _birthYear.value.isBlank()) {
            _signUpState.value = SignUpState.Error("Date of birth is mandatory")
            return
        }

        if (_gender.value.isBlank()) {
            _signUpState.value = SignUpState.Error("Select a genre")
            return
        }

        if (_nationality.value.isBlank()) {
            _signUpState.value = SignUpState.Error("Select a nationality")
            return
        }

        if (_language.value.isBlank()) {
            _signUpState.value = SignUpState.Error("Select a language")
            return
        }

        if (!_isStudent.value && !_isHost.value) {
            _signUpState.value = SignUpState.Error("Select the type of user")
            return
        }

        _signUpState.value = SignUpState.Loading

        viewModelScope.launch {
            val birthDate = "${_birthDay.value}/${_birthMonth.value}/${_birthYear.value}"
            val fullPhoneNumber = "${_phonePrefix.value}${_phoneNumber.value}"

            val result = repository.registerUser(
                email = _email.value.trim(),
                password = _password.value,
                name = _name.value.trim(),
                phoneNumber = fullPhoneNumber,
                gender = _gender.value,
                nationality = _nationality.value,
                language = _language.value,
                birthDate = birthDate,
                isStudent = _isStudent.value
            )

            _signUpState.value = if (result.isSuccess) {
                SignUpState.Success(result.getOrNull()!!)
            } else {
                SignUpState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _signUpState.value = SignUpState.Idle
    }
}

sealed class SignUpState {
    object Idle : SignUpState()
    object Loading : SignUpState()
    data class Success(val userId: String) : SignUpState()
    data class Error(val message: String) : SignUpState()
}