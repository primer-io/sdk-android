package io.primer.android.model

internal data class SyncValidationError(
    val name: String,
    val errorId: Int,
    val fieldId: Int,
)
