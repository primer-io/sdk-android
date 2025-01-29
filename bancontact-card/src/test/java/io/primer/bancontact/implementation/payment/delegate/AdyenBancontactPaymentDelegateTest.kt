package io.primer.bancontact.implementation.payment.delegate

import io.mockk.coEvery
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.PrimerSessionIntent
import io.primer.android.bancontact.implementation.payment.delegate.AdyenBancontactPaymentDelegate
import io.primer.android.bancontact.implementation.payment.resume.handler.AydenBancontactDecision
import io.primer.android.bancontact.implementation.payment.resume.handler.AydenBancontactResumeHandler
import io.primer.android.errors.domain.BaseErrorResolver
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.payments.core.create.domain.handler.PaymentMethodTokenHandler
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import io.primer.android.payments.core.resume.domain.handler.PaymentResumeHandler
import io.primer.android.webRedirectShared.implementation.composer.presentation.WebRedirectLauncherParams
import io.primer.bancontact.implementation.InstantExecutorExtension
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
internal class AdyenBancontactPaymentDelegateTest {
    private lateinit var paymentDelegate: AdyenBancontactPaymentDelegate
    private lateinit var resumeHandler: AydenBancontactResumeHandler

    @BeforeEach
    fun setUp() {
        val paymentMethodTokenHandler = mockk<PaymentMethodTokenHandler>()
        val resumePaymentHandler = mockk<PaymentResumeHandler>()
        val successHandler = mockk<CheckoutSuccessHandler>()
        val errorHandler = mockk<CheckoutErrorHandler>()
        val baseErrorResolver = mockk<BaseErrorResolver>()

        resumeHandler = mockk()

        paymentDelegate =
            AdyenBancontactPaymentDelegate(
                paymentMethodTokenHandler,
                resumePaymentHandler,
                successHandler,
                errorHandler,
                baseErrorResolver,
                resumeHandler,
            )
    }

    @Test
    fun `handleNewClientToken should return success and emit Navigate event when continueWithNewClientToken returns a success result`() =
        runTest {
            // Given
            val clientToken = "testClientToken"
            val decision =
                AydenBancontactDecision(
                    title = "Test Title",
                    paymentMethodType = PaymentMethodType.ADYEN_BANCONTACT_CARD.name,
                    redirectUrl = "https://test.com/redirect",
                    statusUrl = "https://test.com/status",
                    deeplinkUrl = "https://test.com/deeplink",
                )
            coEvery { resumeHandler.continueWithNewClientToken(clientToken) } returns Result.success(decision)

            launch {
                paymentDelegate.handleNewClientToken(clientToken, null)
            }

            paymentDelegate.uiEvent.first { event ->
                val expectedEvent =
                    ComposerUiEvent.Navigate(
                        PaymentMethodLauncherParams(
                            paymentMethodType = PaymentMethodType.ADYEN_BANCONTACT_CARD.name,
                            sessionIntent = PrimerSessionIntent.CHECKOUT,
                            initialLauncherParams =
                            WebRedirectLauncherParams(
                                decision.title,
                                decision.paymentMethodType,
                                decision.redirectUrl,
                                decision.statusUrl,
                                decision.deeplinkUrl,
                            ),
                        ),
                    )
                assertEquals(expectedEvent, event)
                true
            }
        }

    @Test
    fun `handleNewClientToken should handle exception and return failure when continueWithNewClientToken() returns a failure result`() =
        runTest {
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
