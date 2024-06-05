package io.primer.android.model

import androidx.annotation.StringRes

internal data class SyncValidationError(
    val name: String,
    @StringRes val errorResId: Int? = null,
    val fieldId: Int,
    @StringRes val errorFormatId: Int? = null, // Need for align with other platforms, will remove for BA v2
    val errorId: String
)
