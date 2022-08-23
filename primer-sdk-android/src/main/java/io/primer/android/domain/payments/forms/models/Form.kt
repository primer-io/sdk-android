package io.primer.android.domain.payments.forms.models

import io.primer.android.data.payments.forms.models.ButtonType
import io.primer.android.data.payments.forms.models.FormType

internal data class Form(
    val title: Int? = null,
    val logo: Int,
    val buttonType: ButtonType,
    val description: Int? = null,
    val inputs: List<FormInput>? = null,
    val qrCode: String? = null,
    val accountNumber: String? = null,
    val expiration: String? = null,
    val qrCodeUrl: String? = null,
    val inputPrefix: FormInputPrefix? = null,
)

internal data class FormInput(
    val formType: FormType,
    val inputType: Int,
    val id: String,
    val hint: Int,
    val inputCharacters: String?,
    val maxInputLength: Int?,
    val regex: Regex?,
    val inputPrefix: FormInputPrefix?
)

internal interface FormInputPrefix
