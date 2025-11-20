// ABOUTME: Sealed class representing operation results with success or error states
package com.buswatch.util

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}
