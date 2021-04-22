package io.primer.android.model.dto

data class SyncValidationError(
    val name: String,
    val errorId: Int,
    val fieldId: Int,
)
