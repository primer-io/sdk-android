package io.primer.android.domain.payments.forms.validation

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.data.payments.forms.models.FormType
import io.primer.android.domain.payments.forms.models.FormValidationParam
import io.primer.android.domain.payments.forms.validation.iban.IBANChecksumValidator
import io.primer.android.domain.payments.forms.validation.regex.RegexValidator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class ValidationsFactoryTest {

    private lateinit var factory: ValidatorFactory

    @BeforeEach
    fun setUp() {
        factory = ValidatorFactory()
    }

    @Test
    fun `getValidators should return  IBANChecksumValidator and RegexValidator when FormType is IBAN `() {
        val expectedValidators =
            setOf(RegexValidator::class.java, IBANChecksumValidator::class.java)
        val params = mockk<FormValidationParam>(relaxed = true)
        every { params.formType }.returns(FormType.IBAN)

        val validators = factory.getValidators(params)

        assert(expectedValidators == validators.map { it.javaClass }.toSet())
    }

    @Test
    fun `getValidators should return and RegexValidator`() {
        val expectedValidators = setOf(RegexValidator::class.java)
        val params = mockk<FormValidationParam>(relaxed = true)

        val validators = factory.getValidators(params)

        assert(expectedValidators == validators.map { it.javaClass }.toSet())
    }
}
