package io.primer.android.stripe.ach.implementation.session.data.validation.validator

import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(MockKExtension::class)
class EmailAddressValidatorTest {
    @Test
    fun `validate() should return null when email address is valid`() {
        val error = EmailAddressValidator.validate("john@doe.com")
        assertNull(error)
    }

    @Test
    fun `validate() should return PrimerValidationError when email address is not valid`() {
        val error = EmailAddressValidator.validate("john@doe")
        assertNotNull(error)
        assertEquals(StripeAchUserDetailsValidations.INVALID_CUSTOMER_EMAIL_ADDRESS_ERROR_ID, error.errorId)
        assertEquals("The email address is invalid.", error.description)
    }
}
