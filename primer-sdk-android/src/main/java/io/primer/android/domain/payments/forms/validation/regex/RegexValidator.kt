package io.primer.android.domain.payments.forms.validation.regex

import io.primer.android.domain.payments.forms.validation.Validator

internal class RegexValidator(private val regex: Regex?) : Validator {
    override fun validate(input: String?): Boolean {
        return regex?.matches(input.toString().trim()) ?: true
    }
}
