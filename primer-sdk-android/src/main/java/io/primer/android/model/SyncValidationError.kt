package io.primer.android.model

internal data class SyncValidationError(
    val name: String,
    val errorId: Int? = null,
    val fieldId: Int,
    val errorFormatId: Int? = null, // Need for align with other platforms, will remove for BA v2
)
