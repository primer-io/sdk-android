package io.primer.android.ui.core.model

import androidx.annotation.StringRes
import io.primer.android.components.domain.inputs.models.PrimerInputElementType

data class SyncValidationError(
    val inputElementType: PrimerInputElementType,
    val errorId: String,
    val fieldId: Int,
    @StringRes val errorResId: Int? = null,
    @StringRes val errorFormatId: Int? = null,
)
