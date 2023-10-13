package io.primer.android.data.payments.forms.models

import android.text.InputType
import io.primer.android.domain.payments.forms.models.Form
import io.primer.android.domain.payments.forms.models.FormInput
import io.primer.android.domain.payments.forms.models.FormInputPrefix

internal data class FormDataResponse(
    val title: Int? = null,
    val logo: Int,
    val buttonType: ButtonType,
    val description: Int? = null,
    val inputs: List<FormInputDataResponse>? = null,
    val qrCode: String? = null,
    val accountNumber: String? = null,
    val expiration: String? = null,
    val qrCodeUrl: String? = null,
    val inputPrefix: FormInputPrefix? = null,
    val expiresAt: String? = null,
    val reference: String? = null,
    val entity: String? = null
)

internal data class FormInputDataResponse(
    val type: FormType,
    val id: String,
    val hint: Int,
    val level: Int? = null,
    val mask: String? = null,
    val inputCharacters: String? = null,
    val maxInputLength: Int? = null,
    val validation: String? = null,
    val inputPrefix: FormInputPrefix? = null
)

internal enum class FormType {

    TEXT,
    PHONE,
    NUMBER,
    IBAN
}

internal enum class ButtonType {

    CONFIRM,
    PAY,
    NEXT
}

internal fun FormType.toInputType() = when (this) {
    FormType.TEXT -> InputType.TYPE_CLASS_TEXT
    FormType.PHONE -> InputType.TYPE_CLASS_PHONE
    FormType.NUMBER -> InputType.TYPE_CLASS_NUMBER
    FormType.IBAN -> InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
}

internal fun FormDataResponse.toForm() = Form(
    title,
    logo,
    buttonType,
    description,
    inputs?.map { it.toFormInput() },
    qrCode,
    accountNumber,
    expiration,
    qrCodeUrl,
    inputPrefix,
    expiresAt,
    reference,
    entity
)

private fun FormInputDataResponse.toFormInput() = FormInput(
    type,
    type.toInputType(),
    id,
    hint,
    inputCharacters,
    maxInputLength,
    validation?.let { Regex(it, RegexOption.IGNORE_CASE) },
    inputPrefix
)
