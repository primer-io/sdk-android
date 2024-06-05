package io.primer.android.components.data.error

import io.mockk.every
import io.mockk.mockk
import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.exception.InvalidTokenizationDataException
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.error.models.GeneralError
import io.primer.android.domain.error.models.HUCError
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HUCErrorMapperTest {
    private val errorMapper = HUCErrorMapper()

    @Test
    fun `should emit InvalidTokenizationInputDataError when throwable is InvalidTokenizationDataException`() {
        val paymentMethodType = PaymentMethodType.ADYEN_ALIPAY
        val inputData = mockk<KClass<out PrimerRawData>> {
            every { this@mockk.simpleName } returns "simpleName"
        }
        val throwable = InvalidTokenizationDataException(paymentMethodType.name, inputData, mockk())

        val actualResult = errorMapper.getPrimerError(throwable)
        val expectedDescription =
            "PrimerHeadlessUniversalCheckout tokenization error for" +
                " $paymentMethodType and input data ${inputData.simpleName}"
        val expectedContext = ErrorContextParams(
            "invalid-raw-type-data",
            paymentMethodType.name
        )

        assertTrue(actualResult is HUCError.InvalidTokenizationInputDataError)
        assertEquals(expectedDescription, actualResult.description)
        assertEquals(expectedContext, actualResult.context)
    }

    @Test
    fun `should emit GeneralError when throwable is not known mapped Exception`() {
        val message = "message"
        val throwable = Exception(message)

        val actualResult = errorMapper.getPrimerError(throwable)
        val expectedDescription = "Something went wrong. Message $message."

        assertTrue(actualResult is GeneralError.UnknownError)
        assertEquals(expectedDescription, actualResult.description)
    }
}
