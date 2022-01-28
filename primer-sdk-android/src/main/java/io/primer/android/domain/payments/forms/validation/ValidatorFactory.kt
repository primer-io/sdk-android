package io.primer.android.domain.payments.forms.validation

import io.primer.android.data.payments.forms.models.FormType
import io.primer.android.domain.payments.forms.models.FormValidationParam
import io.primer.android.domain.payments.forms.validation.iban.IBANChecksumValidator
import io.primer.android.domain.payments.forms.validation.regex.RegexValidator

internal class ValidatorFactory {

    fun getValidators(formInputParams: FormValidationParam) =
        when (formInputParams.formType) {
            FormType.IBAN -> listOf(
                RegexValidator(formInputParams.regex),
                IBANChecksumValidator()
            )
            else -> listOf(RegexValidator(formInputParams.regex))
        }
}
