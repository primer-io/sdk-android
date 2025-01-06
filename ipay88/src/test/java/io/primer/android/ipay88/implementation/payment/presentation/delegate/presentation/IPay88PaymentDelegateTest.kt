package io.primer.android.ipay88.implementation.payment.presentation.delegate.presentation

import io.mockk.coEvery
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.PrimerSessionIntent
import io.primer.android.errors.domain.BaseErrorResolver
import io.primer.android.ipay88.InstantExecutorExtension
import io.primer.android.ipay88.implementation.composer.presentation.RedirectLauncherParams
import io.primer.android.ipay88.implementation.payment.resume.handler.IPay88Decision
import io.primer.android.ipay88.implementation.payment.resume.handler.IPay88ResumeHandler
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.payments.core.create.domain.handler.PaymentMethodTokenHandler
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import io.primer.android.payments.core.resume.domain.handler.PaymentResumeHandler
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
internal class IPay88PaymentDelegateTest {
    private lateinit var paymentDelegate: IPay88PaymentDelegate
    private lateinit var resumeHandler: IPay88ResumeHandler

    @BeforeEach
    fun setUp() {
        val paymentMethodTokenHandler = mockk<PaymentMethodTokenHandler>()
        val resumePaymentHandler = mockk<PaymentResumeHandler>()
        val successHandler = mockk<CheckoutSuccessHandler>()
        val errorHandler = mockk<CheckoutErrorHandler>()
        val baseErrorResolver = mockk<BaseErrorResolver>()

        resumeHandler = mockk()

        paymentDelegate =
            IPay88PaymentDelegate(
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
                IPay88Decision(
                    statusUrl = "testStatusUrl",
                    iPayPaymentId = "testIPayPaymentId",
                    iPayMethod = 88,
                    merchantCode = "testMerchantCode",
                    actionType = "testActionType",
                    amount = "testAmount",
                    referenceNumber = "testReferenceNumber",
                    prodDesc = "testProdDesc",
                    currencyCode = "testCurrencyCode",
                    countryCode = "testCountryCode",
                    paymentMethodType = PaymentMethodType.GOOGLE_PAY.name,
                    deeplinkUrl = "testDeeplinkUrl",
                    errorCode = 500,
                    sessionIntent = PrimerSessionIntent.CHECKOUT,
                    customerName = "testCustomerName",
                    customerEmail = "testCustomerEmail",
                    remark = "testRemark",
                    backendCallbackUrl = "testBackendCallbackUrl",
                )
            coEvery { resumeHandler.continueWithNewClientToken(clientToken) } returns Result.success(decision)

            launch {
                paymentDelegate.handleNewClientToken(clientToken, null)
            }

            paymentDelegate.uiEvent.first { event ->
                val expectedEvent =
                    ComposerUiEvent.Navigate(
                        PaymentMethodLauncherParams(
                            paymentMethodType = PaymentMethodType.GOOGLE_PAY.name,
                            sessionIntent = PrimerSessionIntent.CHECKOUT,
                            initialLauncherParams =
                                RedirectLauncherParams(
                                    statusUrl = decision.statusUrl,
                                    iPayPaymentId = decision.iPayPaymentId,
                                    iPayMethod = decision.iPayMethod,
                                    merchantCode = decision.merchantCode,
                                    actionType = decision.actionType,
                                    amount = decision.amount,
                                    referenceNumber = decision.referenceNumber,
                                    prodDesc = decision.prodDesc,
                                    currencyCode = decision.currencyCode,
                                    countryCode = decision.countryCode,
                                    customerName = decision.customerName,
                                    customerEmail = decision.customerEmail,
                                    remark = decision.remark,
                                    backendCallbackUrl = decision.backendCallbackUrl,
                                    deeplinkUrl = decision.deeplinkUrl,
                                    errorCode = decision.errorCode,
                                    paymentMethodType = decision.paymentMethodType,
                                    sessionIntent = decision.sessionIntent,
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
