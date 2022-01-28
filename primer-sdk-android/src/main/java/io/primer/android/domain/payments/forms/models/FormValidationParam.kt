package io.primer.android.domain.payments.forms.models

import io.primer.android.data.payments.forms.models.FormType
import io.primer.android.domain.base.Params

internal data class FormValidationParam(
    val input: CharSequence?,
    val formType: FormType,
    val regex: Regex?
) : Params
