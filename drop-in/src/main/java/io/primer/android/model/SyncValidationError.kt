package io.primer.android.model

import androidx.annotation.StringRes
import io.primer.android.components.domain.inputs.models.PrimerInputElementType

internal data class SyncValidationError(
    val inputElementType: PrimerInputElementType,
    val errorId: String,
    val fieldId: Int,
    @StringRes val errorResId: Int? = null,
    @StringRes val errorFormatId: Int? = null // Need for align with other platforms, will remove for BA v2
)
