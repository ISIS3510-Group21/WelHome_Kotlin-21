package com.team21.myapplication.ui.createAccountView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.data.repository.AuthRepository
import com.team21.myapplication.ui.createAccountView.state.OperationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.team21.myapplication.ui.createAccountView.state.SignUpUiState
import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.TimeZone

class SignUpViewModel : ViewModel() {
    private val repository = AuthRepository()

    // unique stateFflow that contains the complete stage
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    // Update functions - actualizan el estado unificado
    fun updateName(value: String) {
        _uiState.value = _uiState.value.copy(name = value)
    }

    fun updateEmail(value: String) {
        _uiState.value = _uiState.value.copy(email = value)
    }

    fun updatePassword(value: String) {
        _uiState.value = _uiState.value.copy(password = value)
    }

    fun updatePhoneNumber(value: String) {
        _uiState.value = _uiState.value.copy(phoneNumber = value)
    }

    fun updatePhonePrefix(value: String) {
        _uiState.value = _uiState.value.copy(phonePrefix = value)
    }

    fun updateBirthDay(value: String) {
        _uiState.value = _uiState.value.copy(birthDay = value)
    }

    fun updateBirthMonth(value: String) {
        _uiState.value = _uiState.value.copy(birthMonth = value)
    }

    fun updateBirthYear(value: String) {
        _uiState.value = _uiState.value.copy(birthYear = value)
    }

    fun updateGender(value: String) {
        _uiState.value = _uiState.value.copy(gender = value)
    }

    fun updateNationality(value: String) {
        _uiState.value = _uiState.value.copy(nationality = value)
    }

    fun updateLanguage(value: String) {
        _uiState.value = _uiState.value.copy(language = value)
    }

    fun toggleUserType(isStudentSelected: Boolean) {
        _uiState.value = _uiState.value.copy(
            isStudent = isStudentSelected,
            isHost = !isStudentSelected
        )
    }

    fun signUp() {
        // Validar usando el estado actual
        val validationError = validateSignUpState(_uiState.value)
        if (validationError != null) {
            _uiState.value = _uiState.value.copy(
                operationState = OperationState.Error(validationError)
            )
            return
        }

        _uiState.value = _uiState.value.copy(operationState = OperationState.Loading)

        viewModelScope.launch {
            val state = _uiState.value
            val fullPhoneNumber = "${state.phonePrefix}${state.phoneNumber}"
            val birthRes = buildBirthTimestampOrError(state)

            if (birthRes.isFailure) {
                _uiState.value = _uiState.value.copy(
                    operationState = OperationState.Error(birthRes.exceptionOrNull()?.message ?: "Invalid birth date")
                )
                return@launch
            }

            val birthDate = birthRes.getOrThrow()

            val result = repository.registerUser(
                email = state.email.trim(),
                password = state.password,
                name = state.name.trim(),
                phoneNumber = fullPhoneNumber,
                gender = state.gender,
                nationality = state.nationality,
                language = state.language,
                birthDate = birthDate,
                isStudent = state.isStudent
            )

            _uiState.value = _uiState.value.copy(
                operationState = if (result.isSuccess) {
                    OperationState.Success(result.getOrNull()!!)
                } else {
                    OperationState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = _uiState.value.copy(operationState = OperationState.Idle)
    }

    // Función pura de validación
    private fun validateSignUpState(state: SignUpUiState): String? {
        return when {
            state.name.isBlank() -> "Name is mandatory"
            state.email.isBlank() || !state.email.contains("@") -> "Enter a valid email address"
            state.password.length < 6 -> "The password must be at least 6 characters long"
            state.phoneNumber.isBlank() -> "The phone number is required"
            state.birthDay.isBlank() || state.birthMonth.isBlank() || state.birthYear.isBlank() ->
                "Date of birth is mandatory"
            state.gender.isBlank() -> "Select a genre"
            state.nationality.isBlank() -> "Select a nationality"
            state.language.isBlank() -> "Select a language"
            !state.isStudent && !state.isHost -> "Select the type of user"
            else -> null
        }
    }

    // Valida y construye el Timestamp de nacimiento
    private fun buildBirthTimestampOrError(state: SignUpUiState): Result<Timestamp> {
        val day = state.birthDay.trim().toIntOrNull()
            ?: return Result.failure(IllegalArgumentException("Day must be a number"))
        val month = state.birthMonth.trim().toIntOrNull()
            ?: return Result.failure(IllegalArgumentException("Month must be a number"))
        val year = state.birthYear.trim().toIntOrNull()
            ?: return Result.failure(IllegalArgumentException("Year must be a number"))

        if (month !in 1..12) return Result.failure(IllegalArgumentException("Month must be between 1 and 12"))
        if (day !in 1..31)  return Result.failure(IllegalArgumentException("Day must be between 1 and 31"))
        if (year !in 1900..Calendar.getInstance().get(Calendar.YEAR))
            return Result.failure(IllegalArgumentException("Enter a valid year"))

        val tz = TimeZone.getTimeZone("GMT-4")
        val cal = Calendar.getInstance(tz).apply {
            isLenient = false
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return try {
            Result.success(Timestamp(cal.time))
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Enter a valid date"))
        }
    }

}
