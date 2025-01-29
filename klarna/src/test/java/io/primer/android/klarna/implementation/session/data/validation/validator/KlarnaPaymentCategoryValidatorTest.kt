package io.primer.android.klarna.implementation.session.data.validation.validator

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.klarna.implementation.session.domain.models.KlarnaPaymentCategory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
class KlarnaPaymentCategoryValidatorTest {
    @Test
    fun `validate() should return PrimerValidationError when 'banks' is null`() {
        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns "uuid"
        val result =
            KlarnaPaymentCategoryValidator.validate(
                paymentCategories = null,
                paymentCategory = mockk(),
            )

        assertEquals(
            PrimerValidationError(
                errorId = KlarnaValidations.SESSION_NOT_CREATED_ERROR_ID,
                description =
                "Session needs to be created before payment category can " +
                    "be collected.",
                diagnosticsId = "uuid",
            ),
            result,
        )
        unmockkStatic(UUID::class)
    }

    @Test
    fun `validate() should return PrimerValidationError when there's no matching payment category`() {
        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns "uuid"
        val paymentCategories = listOf(mockk<KlarnaPaymentCategory>())
        val result =
            KlarnaPaymentCategoryValidator.validate(
                paymentCategories = paymentCategories,
                paymentCategory = mockk(),
            )

        assertEquals(
            PrimerValidationError(
                errorId = KlarnaValidations.INVALID_PAYMENT_CATEGORY_ERROR_ID,
                description = "Payment category is invalid.",
                diagnosticsId = "uuid",
            ),
            result,
        )
        unmockkStatic(UUID::class)
    }

    @Test
    fun `validate() should return null when there's a matching payment category`() {
        val paymentCategory = mockk<KlarnaPaymentCategory>()
        val paymentCategories = listOf(paymentCategory)
        val result =
            KlarnaPaymentCategoryValidator.validate(
                paymentCategories = paymentCategories,
                paymentCategory = paymentCategory,
            )

        assertNull(result)
    }
}
