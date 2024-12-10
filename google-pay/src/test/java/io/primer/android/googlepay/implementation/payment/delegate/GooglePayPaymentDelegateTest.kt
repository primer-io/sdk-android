package io.primer.android.googlepay.implementation.payment.delegate

import io.mockk.coEvery
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.PrimerSessionIntent
import io.primer.android.errors.domain.BaseErrorResolver
import io.primer.android.googlepay.InstantExecutorExtension
import io.primer.android.googlepay.implementation.payment.resume.handler.GooglePayResumeDecision
import io.primer.android.googlepay.implementation.payment.resume.handler.GooglePayResumeHandler
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.payments.core.create.domain.handler.PaymentMethodTokenHandler
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import io.primer.android.payments.core.resume.domain.handler.PaymentResumeHandler
import io.primer.android.processor3ds.domain.model.Processor3DS
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodLauncherParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
internal class GooglePayPaymentDelegateTest {

    private lateinit var paymentDelegate: GooglePayPaymentDelegate
    private lateinit var resumeHandler: GooglePayResumeHandler

    @BeforeEach
    fun setUp() {
        val paymentMethodTokenHandler = mockk<PaymentMethodTokenHandler>()
        val resumePaymentHandler = mockk<PaymentResumeHandler>()
        val successHandler = mockk<CheckoutSuccessHandler>()
        val errorHandler = mockk<CheckoutErrorHandler>()
        val baseErrorResolver = mockk<BaseErrorResolver>()

        resumeHandler = mockk()

        paymentDelegate = GooglePayPaymentDelegate(
            paymentMethodTokenHandler,
            resumePaymentHandler,
            successHandler,
            errorHandler,
            baseErrorResolver,
            resumeHandler
        )
    }

    @Test
    fun `handleNewClientToken should return success and emit Navigate to Native 3ds launcher event when continueWithNewClientToken returns a success result`() = runTest {
        // Given
        val clientToken = "testClientToken"
        val decision = GooglePayResumeDecision.GooglePayNative3dsResumeDecision(listOf("1.0", "2.0"))
        coEvery { resumeHandler.continueWithNewClientToken(clientToken) } returns Result.success(decision)

        launch {
            paymentDelegate.handleNewClientToken(clientToken, null)
        }

        paymentDelegate.uiEvent.first { event ->
            val expectedEvent = ComposerUiEvent.Navigate(
                PaymentMethodLauncherParams(
                    paymentMethodType = PaymentMethodType.GOOGLE_PAY.name,
                    sessionIntent = PrimerSessionIntent.CHECKOUT,
                    initialLauncherParams = ThreeDsInitialLauncherParams(
                        supportedThreeDsProtocolVersions = decision.supportedThreeDsProtocolVersions
                    )
                )
            )
            assertEquals(expectedEvent, event)
            true
        }
    }

    @Test
    fun `handleNewClientToken should return success and emit Navigate to Processor 3ds launcher event when continueWithNewClientToken returns a success result`() = runTest {
        // Given
        val clientToken = "testClientToken"
        val decision = GooglePayResumeDecision.GooglePayProcessor3dsResumeDecision(
            processor3DS = Processor3DS(
                redirectUrl = "https://www.example/redirect",
                statusUrl = "https://www.status/redirect",
                title = "title"
            )
        )
        coEvery { resumeHandler.continueWithNewClientToken(clientToken) } returns Result.success(decision)

        launch {
            paymentDelegate.handleNewClientToken(clientToken, null)
        }

        paymentDelegate.uiEvent.first { event ->
            val expectedEvent = ComposerUiEvent.Navigate(
                PaymentMethodLauncherParams(
                    paymentMethodType = PaymentMethodType.GOOGLE_PAY.name,
                    sessionIntent = PrimerSessionIntent.CHECKOUT,
                    initialLauncherParams = ProcessorThreeDsInitialLauncherParams(
                        processor3DS = decision.processor3DS
                    )
                )
            )
            assertEquals(expectedEvent, event)
            true
        }
    }

    @Test
    fun `handleNewClientToken should handle exception and return failure when continueWithNewClientToken() returns a failure result`() = runTest {
        // Given
        val clientToken = "testClientToken"
        val exception = Exception("Test Exception")
        coEvery { resumeHandler.continueWithNewClientToken(clientToken) } returns Result.failure(exception)

        // When
        val result = paymentDelegate.handleNewClientToken(clientToken, null)

        // Then
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
