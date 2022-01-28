package io.primer.android.domain.payments.forms.validation

internal interface Validator {

    fun validate(input: String?): Boolean
}
