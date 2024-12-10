package io.primer.android.stripe.ach.implementation.session.data.validation.validator

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.primer.android.components.domain.error.PrimerValidationError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
class LastNameValidatorTest {
    @Test
    fun `validate() should return PrimerValidationError when last name is blank`() {
        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns "uuid"
        val result = LastNameValidator.validate(value = " ")

        assertEquals(
            PrimerValidationError(
                errorId = StripeAchUserDetailsValidations.INVALID_CUSTOMER_LAST_NAME_ERROR_ID,
                description = "The last name may not be blank.",
                diagnosticsId = "uuid"
            ),
            result
        )
        unmockkStatic(UUID::class)
    }

    @Test
    fun `validate() should return when last name is not blank`() {
        val result = LastNameValidator.validate(value = "Doe")
        assertNull(result)
    }
}
