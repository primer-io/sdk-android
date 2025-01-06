package io.primer.android.klarna.implementation.session.data.validation.validator

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
class KlarnaPaymentFinalizationValidatorTest {
    @Test
    fun `validate() should return PrimerValidationError when 'isFinalizationRequired' is false`() {
        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns "uuid"
        val result = KlarnaPaymentFinalizationValidator.validate(isFinalizationRequired = false)

        assertEquals(
            PrimerValidationError(
                errorId = KlarnaValidations.PAYMENT_ALREADY_FINALIZED_ERROR_ID,
                description = "This payment was configured to finalized automatically.",
                diagnosticsId = "uuid",
            ),
            result,
        )
        unmockkStatic(UUID::class)
    }

    @Test
    fun `validate() should return null when 'isFinalizationRequired' is true`() {
        val result = KlarnaPaymentFinalizationValidator.validate(isFinalizationRequired = true)

        assertNull(result)
    }
}
