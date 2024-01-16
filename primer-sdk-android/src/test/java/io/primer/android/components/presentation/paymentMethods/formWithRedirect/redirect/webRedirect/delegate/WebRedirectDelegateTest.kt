package io.primer.android.components.presentation.paymentMethods.formWithRedirect.redirect.webRedirect.delegate

import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.InstantExecutorExtension
import io.primer.android.components.manager.redirect.composable.WebRedirectStep
import io.primer.android.domain.error.models.PaymentMethodError
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.extensions.collectIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.time.Duration.Companion.seconds

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
internal class WebRedirectDelegateTest {

    private val webRedirectDelegate = WebRedirectDelegate()

    @ParameterizedTest
    @MethodSource("provideErrorCheckoutEvents")
    fun `errors() emits only CheckoutError and CheckoutPaymentError that don't signal cancellation when called`(
        pair: Pair<CheckoutEvent, Boolean>
    ) = runTest {
        val (event, isEmitted) = pair
        val errors = mutableListOf<PrimerError>()
        val job = webRedirectDelegate.errors().collectIn(errors, this)
        delay(0.5.seconds)
        val errorCount = errors.size
        EventBus.broadcast(event)
        delay(0.5.seconds)
        assertEquals(if (isEmitted) errorCount + 1 else errorCount, errors.size)
        job.cancel()
    }

    @ParameterizedTest
    @MethodSource("provideStepCheckoutEvents")
    fun `steps() emits only PaymentMethodPresented, PaymentSuccess, and CheckoutError and CheckoutPaymentError that signal cancellation when called`(
        pair: Pair<CheckoutEvent, WebRedirectStep?>
    ) = runTest {
        val (event, step) = pair
        val steps = mutableListOf<WebRedirectStep>()
        val job = webRedirectDelegate.steps().collectIn(steps, this)
        delay(0.5.seconds)
        val stepCount = steps.size
        EventBus.broadcast(event)
        delay(0.5.seconds)
        assertEquals(if (step != null) stepCount + 1 else stepCount, steps.size)
        if (step != null) {
            assertEquals(step, steps.last())
        }
        job.cancel()
    }

    companion object {
        @JvmStatic
        fun provideErrorCheckoutEvents() = listOf<Pair<CheckoutEvent, Boolean>>(
            CheckoutEvent.CheckoutError(
                PaymentMethodError.PaymentMethodCancelledError("")
            ) to false,
            CheckoutEvent.CheckoutPaymentError(
                PaymentMethodError.PaymentMethodCancelledError("")
            ) to false,
            CheckoutEvent.CheckoutError(mockk()) to true,
            CheckoutEvent.CheckoutPaymentError(mockk()) to true
        )

        @JvmStatic
        fun provideStepCheckoutEvents() = listOf<Pair<CheckoutEvent, WebRedirectStep?>>(
            CheckoutEvent.PaymentMethodPresented("") to WebRedirectStep.Loaded,
            CheckoutEvent.PaymentSuccess(mockk()) to WebRedirectStep.Success,
            CheckoutEvent.CheckoutError(
                PaymentMethodError.PaymentMethodCancelledError("")
            ) to WebRedirectStep.Dismissed,
            CheckoutEvent.CheckoutPaymentError(
                PaymentMethodError.PaymentMethodCancelledError("")
            ) to WebRedirectStep.Dismissed,
            CheckoutEvent.CheckoutError(mockk()) to null,
            CheckoutEvent.CheckoutPaymentError(mockk()) to null
        )
    }
}
