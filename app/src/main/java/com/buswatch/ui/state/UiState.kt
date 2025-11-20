// ABOUTME: Generic sealed class for representing UI loading, success, and error states
package com.buswatch.ui.state

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val canRetry: Boolean = true) : UiState<Nothing>()
}
