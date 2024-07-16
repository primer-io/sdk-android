package io.primer.android.components.data.payments.paymentMethods.nativeUi.stripe.ach.error

import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.error.models.GeneralError
import io.primer.android.domain.error.models.StripeError
import io.primer.android.stripe.exceptions.StripePublishableKeyMismatchException
import io.primer.android.stripe.exceptions.StripeSdkException
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StripeErrorMapperTest {
    private val errorMapper = StripeErrorMapper()

    @Test
    fun `getPrimerError() should return StripeInvalidPublishableKeyError error when called with StripePublishableKeyMismatchException`() {
        val throwable = StripePublishableKeyMismatchException("message")

        val actualResult = errorMapper.getPrimerError(throwable)
        val expectedDescription =
            "Publishable key is invalid"
        val expectedContext = ErrorContextParams(
            "stripe-invalid-publishable-key",
            PaymentMethodType.STRIPE_ACH.name
        )

        assertTrue(actualResult is StripeError.StripeInvalidPublishableKeyError)
        assertEquals(expectedDescription, actualResult.description)
        assertEquals(expectedContext, actualResult.context)
    }

    @Test
    fun `getPrimerError() should return StripeSdkError error when called with StripeSdkException`() {
        val message = "message"
        val throwable = StripeSdkException(message)

        val actualResult = errorMapper.getPrimerError(throwable)
        val expectedDescription =
            "Multiple errors occurred: $message"
        val expectedContext = ErrorContextParams(
            "stripe-sdk-error",
            PaymentMethodType.STRIPE_ACH.name
        )

        assertTrue(actualResult is StripeError.StripeSdkError)
        assertEquals(expectedDescription, actualResult.description)
        assertEquals(expectedContext, actualResult.context)
        assertNull(actualResult.errorCode)
        assertEquals(actualResult, actualResult.exposedError)
        assertNull(actualResult.recoverySuggestion)
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
