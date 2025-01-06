package io.primer.android.nolpay.implementation.paymentCard.payment.delegate

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.errors.domain.BaseErrorResolver
import io.primer.android.nolpay.InstantExecutorExtension
import io.primer.android.nolpay.api.manager.payment.composable.NolPayPaymentCollectableData
import io.primer.android.nolpay.api.manager.payment.composable.NolPayPaymentStep
import io.primer.android.nolpay.implementation.paymentCard.completion.domain.NolPayCompletePaymentInteractor
import io.primer.android.nolpay.implementation.paymentCard.completion.domain.NolPayRequestPaymentInteractor
import io.primer.android.nolpay.implementation.paymentCard.completion.domain.model.NolPayCompletePaymentParams
import io.primer.android.nolpay.implementation.paymentCard.completion.domain.model.NolPayRequestPaymentParams
import io.primer.android.nolpay.implementation.paymentCard.payment.resume.handler.NolPayResumeDecision
import io.primer.android.nolpay.implementation.paymentCard.payment.resume.handler.NolPayResumeHandler
import io.primer.android.nolpay.toListDuring
import io.primer.android.payments.core.create.domain.handler.PaymentMethodTokenHandler
import io.primer.android.payments.core.create.domain.model.PaymentDecision
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import io.primer.android.payments.core.resume.domain.handler.PaymentResumeHandler
import io.primer.android.payments.core.status.domain.AsyncPaymentMethodPollingInteractor
import io.primer.android.payments.core.status.domain.model.AsyncStatus
import io.primer.android.payments.core.status.domain.model.AsyncStatusParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.seconds

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
internal class NolPayPaymentDelegateTest {
    private lateinit var delegate: NolPayPaymentDelegate

    private val requestPaymentInteractor: NolPayRequestPaymentInteractor = mockk()
    private val completePaymentInteractor: NolPayCompletePaymentInteractor = mockk()
    private val pollingInteractor: AsyncPaymentMethodPollingInteractor = mockk()
    private val paymentMethodTokenHandler: PaymentMethodTokenHandler = mockk()
    private val resumePaymentHandler: PaymentResumeHandler = mockk()
    private val successHandler: CheckoutSuccessHandler = mockk()
    private val errorHandler: CheckoutErrorHandler = mockk(relaxed = true)
    private val baseErrorResolver: BaseErrorResolver = mockk(relaxed = true)
    private val resumeHandler: NolPayResumeHandler = mockk()

    @BeforeEach
    fun setUp() {
        delegate =
            NolPayPaymentDelegate(
                requestPaymentInteractor,
                completePaymentInteractor,
                pollingInteractor,
                paymentMethodTokenHandler,
                resumePaymentHandler,
                successHandler,
                errorHandler,
                baseErrorResolver,
                resumeHandler,
            )
    }

    @Test
    fun `handleNewClientToken should emit CollectTagData step`() =
        runTest {
            // Given
            val clientToken = "testClientToken"
            val resumeDecision =
                NolPayResumeDecision(
                    transactionNumber = "testTransactionNumber",
                    completeUrl = "https://example.com/complete",
                    statusUrl = "https://example.com/status",
                )

            coEvery { resumeHandler.continueWithNewClientToken(clientToken) } returns Result.success(resumeDecision)

            launch {
                delegate.handleNewClientToken(clientToken, null)
            }

            // Then
            delegate.componentStep.first { event ->
                val expectedEvent = NolPayPaymentStep.CollectTagData
                assertEquals(expectedEvent, event)
                true
            }
        }

    @Test
    fun `requestPayment should call requestPaymentInteractor and completePaymentInteractor and emit PaymentRequested step`() {
        // Given
        val clientToken = "testClientToken"
        val collectedData = NolPayPaymentCollectableData.NolPayTagData(mockk())
        val resumeDecision =
            NolPayResumeDecision(
                transactionNumber = "testTransactionNumber",
                completeUrl = "https://example.com/complete",
                statusUrl = "https://example.com/status",
            )

        val requestParams =
            NolPayRequestPaymentParams(
                tag = collectedData.tag,
                transactionNo = resumeDecision.transactionNumber,
            )
        val completeParams = NolPayCompletePaymentParams(resumeDecision.completeUrl)

        coEvery { requestPaymentInteractor(requestParams) } returns Result.success(true)
        coEvery { completePaymentInteractor(completeParams) } returns Result.success(Unit)
        coEvery { pollingInteractor(any()) } returns flowOf(AsyncStatus(""))
        coEvery { resumeHandler.continueWithNewClientToken(clientToken) } returns Result.success(resumeDecision)
        coEvery {
            resumePaymentHandler.handle(
                any(),
                any(),
            )
        } returns Result.success(PaymentDecision.Pending(clientToken, null))

        // When
        runTest {
            launch {
                delegate.handleNewClientToken(clientToken, null)
                delegate.requestPayment(collectedData)
                delegate.completePayment()
            }

            val events = delegate.componentStep.toListDuring(1.0.seconds)
            assertTrue(events.any { it is NolPayPaymentStep.PaymentRequested })
        }

        // Then
        coVerify { requestPaymentInteractor(requestParams) }
        coVerify { completePaymentInteractor(completeParams) }
    }

    @Test
    fun `requestPayment should handle error from requestPaymentInteractor`() =
        runTest {
            // Given
            val clientToken = "testClientToken"
            val resumeDecision =
                NolPayResumeDecision(
                    transactionNumber = "testTransactionNumber",
                    completeUrl = "https://example.com/complete",
                    statusUrl = "https://example.com/status",
                )

            val collectedData = NolPayPaymentCollectableData.NolPayTagData(mockk())
            val requestParams =
                NolPayRequestPaymentParams(
                    tag = collectedData.tag,
                    transactionNo = resumeDecision.transactionNumber,
                )

            val exception = Exception("Test exception")
            coEvery { requestPaymentInteractor(requestParams) } returns Result.failure(exception)

            coEvery { resumeHandler.continueWithNewClientToken(clientToken) } returns Result.success(resumeDecision)

            delegate.handleNewClientToken(clientToken, null)

            // When/Then
            assertFailsWith<Exception> {
                delegate.requestPayment(collectedData).getOrThrow()
            }

            coVerify { requestPaymentInteractor(requestParams) }
        }

    @Test
    fun `completePayment should handle error from completePaymentInteractor`() =
        runTest {
            // Given
            val clientToken = "testClientToken"
            val resumeDecision =
                NolPayResumeDecision(
                    transactionNumber = "testTransactionNumber",
                    completeUrl = "https://example.com/complete",
                    statusUrl = "https://example.com/status",
                )

            val collectedData = NolPayPaymentCollectableData.NolPayTagData(mockk())
            val requestParams =
                NolPayRequestPaymentParams(
                    tag = collectedData.tag,
                    transactionNo = resumeDecision.transactionNumber,
                )

            val exception = Exception("Test exception")
            coEvery { requestPaymentInteractor(requestParams) } returns Result.success(true)
            coEvery { completePaymentInteractor(NolPayCompletePaymentParams(resumeDecision.completeUrl)) } returns
                Result.failure(exception)

            coEvery { resumeHandler.continueWithNewClientToken(clientToken) } returns Result.success(resumeDecision)

            delegate.handleNewClientToken(clientToken, null)

            // When/Then
            assertFailsWith<Exception> {
                delegate.completePayment().getOrThrow()
            }

            coVerify { completePaymentInteractor(NolPayCompletePaymentParams(resumeDecision.completeUrl)) }
        }

    @Test
    fun `complete should propagate error to error handler from pollingInteractor`() {
        // Given
        val clientToken = "testClientToken"
        val statusUrl = "https://example.com/status"
        val resumeDecision =
            NolPayResumeDecision(
                transactionNumber = "testTransactionNumber",
                completeUrl = "https://example.com/complete",
                statusUrl = statusUrl,
            )

        val collectedData = NolPayPaymentCollectableData.NolPayTagData(mockk())
        val requestParams =
            NolPayRequestPaymentParams(
                tag = collectedData.tag,
                transactionNo = resumeDecision.transactionNumber,
            )
        val pollingParams =
            AsyncStatusParams(
                url = statusUrl,
                paymentMethodType = "NOL_PAY",
            )

        val exception = IllegalStateException("Test exception")
        coEvery { requestPaymentInteractor(requestParams) } returns Result.success(true)
        coEvery { completePaymentInteractor(NolPayCompletePaymentParams(resumeDecision.completeUrl)) } returns
            Result.success(Unit)
        coEvery {
            pollingInteractor(
                params = pollingParams,
            )
        } returns flow { throw exception }

        coEvery { resumeHandler.continueWithNewClientToken(clientToken) } returns Result.success(resumeDecision)
        every { baseErrorResolver.resolve(any()) } returns mockk()

        runTest {
            launch {
                delegate.handleNewClientToken(clientToken, null)
                delegate.completePayment().getOrThrow()
            }
            // When/Then
        }

        coVerify { errorHandler.handle(any(), any()) }
        coVerify { pollingInteractor(pollingParams) }
        coVerify { completePaymentInteractor(NolPayCompletePaymentParams(resumeDecision.completeUrl)) }
    }
}
