package io.primer.android

import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.completion.PrimerErrorDecisionHandler
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.stripe.ach.implementation.errors.domain.model.StripeError
import org.junit.jupiter.api.Test

class PrimerCheckoutListenerTest {
    private val listener = object : PrimerCheckoutListener {
        override fun onCheckoutCompleted(checkoutData: PrimerCheckoutData) {
            // no-op
        }
    }

    @Test
    fun `onFailed(PrimerError, PrimerErrorDecisionHandler) should not call showErrorMessage() when errorHandler is null`() {
        val error = mockk<PrimerError>()

        listener.onFailed(error = error, errorHandler = null)

        confirmVerified(error)
    }

    @Test
    fun `onFailed(PrimerError, PrimerErrorDecisionHandler) should call showErrorMessage() with description when error is StripeError`() {
        val error = StripeError.StripeSdkError(message = "message")
        val errorHandler = mockk<PrimerErrorDecisionHandler>(relaxed = true)

        listener.onFailed(error = error, errorHandler = errorHandler)

        verify { errorHandler.showErrorMessage(error.description) }
    }

    @Test
    fun `onFailed(PrimerError, PrimerErrorDecisionHandler) should call showErrorMessage() with null when error is not StripeError`() {
        val error = mockk<PrimerError>()
        val errorHandler = mockk<PrimerErrorDecisionHandler>(relaxed = true)

        listener.onFailed(error = error, errorHandler = errorHandler)

        verify { errorHandler.showErrorMessage(null) }
    }

    @Test
    fun `onFailed(PrimerError, PrimerCheckoutData, PrimerErrorDecisionHandler) should not call showErrorMessage() when errorHandler is null`() {
        val error = mockk<PrimerError>()

        listener.onFailed(error = error, checkoutData = null, errorHandler = null)

        confirmVerified(error)
    }

    @Test
    fun `onFailed(PrimerError, PrimerCheckoutData, PrimerErrorDecisionHandler) should call showErrorMessage() with description when error is StripeError`() {
        val error = io.primer.android.stripe.ach.implementation.errors.domain.model.StripeError.StripeSdkError(
            message = "message"
        )
        val errorHandler = mockk<PrimerErrorDecisionHandler>(relaxed = true)

        listener.onFailed(error = error, checkoutData = null, errorHandler = errorHandler)

        verify { errorHandler.showErrorMessage(error.description) }
    }

    @Test
    fun `onFailed(PrimerError, PrimerCheckoutData, PrimerErrorDecisionHandler) should call showErrorMessage() with null when error is not StripeError`() {
        val error = mockk<PrimerError>()
        val errorHandler = mockk<PrimerErrorDecisionHandler>(relaxed = true)

        listener.onFailed(error = error, checkoutData = null, errorHandler = errorHandler)

        verify { errorHandler.showErrorMessage(null) }
    }
}
