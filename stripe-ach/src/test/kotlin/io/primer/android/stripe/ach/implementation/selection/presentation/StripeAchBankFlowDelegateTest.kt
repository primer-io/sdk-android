package io.primer.android.stripe.ach.implementation.selection.presentation

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.primer.android.core.extensions.toIso8601String
import io.primer.android.errors.data.exception.PaymentMethodCancelledException
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.payments.core.create.domain.repository.PaymentResultRepository
import io.primer.android.payments.core.helpers.CheckoutAdditionalInfoHandler
import io.primer.android.stripe.ach.api.additionalInfo.AchAdditionalInfo
import io.primer.android.stripe.ach.implementation.mandate.presentation.StripeAchMandateTimestampLoggingDelegate
import io.primer.android.stripe.ach.implementation.payment.confirmation.presentation.CompleteStripeAchPaymentSessionDelegate
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Date
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.seconds

@ExtendWith(MockKExtension::class)
class StripeAchBankFlowDelegateTest {
    @MockK
    private lateinit var stripeAchBankSelectionHandler: StripeAchBankSelectionHandler

    @MockK
    private lateinit var checkoutAdditionalInfoHandler: CheckoutAdditionalInfoHandler

    @MockK
    private lateinit var completeStripeAchPaymentSessionDelegate:
        CompleteStripeAchPaymentSessionDelegate

    @MockK
    private lateinit var stripeAchMandateTimestampLoggingDelegate:
        StripeAchMandateTimestampLoggingDelegate

    @MockK
    private lateinit var paymentResultRepository: PaymentResultRepository

    @InjectMockKs
    private lateinit var resolver: StripeAchBankFlowDelegate

    @AfterEach
    fun tearDown() {
        confirmVerified(
            stripeAchBankSelectionHandler,
            checkoutAdditionalInfoHandler,
            completeStripeAchPaymentSessionDelegate,
            stripeAchMandateTimestampLoggingDelegate,
            paymentResultRepository,
        )
    }

    @Test
    fun `handle() should return result when onAcceptMandate() is called and delegate calls succeed`() = runTest {
        val dateSlot = slot<Date>()
        coEvery {
            stripeAchBankSelectionHandler.fetchSelectedBankId(any())
        } returns Result.success("paymentMethodId")
        coEvery {
            stripeAchMandateTimestampLoggingDelegate.logTimestamp(
                stripePaymentIntentId = any(),
                date = capture(dateSlot)
            )
        } just Runs
        coEvery {
            completeStripeAchPaymentSessionDelegate.invoke(
                completeUrl = any(),
                paymentMethodId = any(),
                mandateTimestamp = any()
            )
        } returns Result.success(Unit)
        val additionalInfoSlot = slot<AchAdditionalInfo.DisplayMandate>()
        coEvery { checkoutAdditionalInfoHandler.handle(capture(additionalInfoSlot)) } just Runs
        val payment = mockk<Payment>()
        every { paymentResultRepository.getPaymentResult().payment } returns payment

        val result = async {
            resolver.handle(
                clientSecret = "clientSecret",
                paymentIntentId = "paymentMethodId",
                sdkCompleteUrl = "sdkCompleteUrl"
            )
        }
        delay(1.seconds)
        val acceptJob = launch {
            additionalInfoSlot.captured.onAcceptMandate()
        }

        acceptJob.join()
        assertEquals(
            StripeAchBankFlowDelegate.StripeAchBankFlowResult(payment, dateSlot.captured.toIso8601String()),
            result.await().getOrNull()
        )
        coVerify {
            stripeAchBankSelectionHandler.fetchSelectedBankId("clientSecret")
            stripeAchMandateTimestampLoggingDelegate.logTimestamp(
                stripePaymentIntentId = "paymentMethodId",
                date = dateSlot.captured
            )
            completeStripeAchPaymentSessionDelegate(
                completeUrl = "sdkCompleteUrl",
                paymentMethodId = "paymentMethodId",
                mandateTimestamp = dateSlot.captured
            )
            checkoutAdditionalInfoHandler.handle(additionalInfoSlot.captured)
        }
        verify {
            paymentResultRepository.getPaymentResult().payment
        }
    }

    @Test
    fun `handle() should return failure when onAcceptMandate() is called and bank selection handler call fails`() = runTest {
        val error = Exception()
        coEvery {
            stripeAchBankSelectionHandler.fetchSelectedBankId(any())
        } returns Result.failure(error)

        val result = resolver.handle(
            clientSecret = "clientSecret",
            paymentIntentId = "paymentMethodId",
            sdkCompleteUrl = "sdkCompleteUrl"
        )

        assertIs<Exception>(result.exceptionOrNull())
        coVerify {
            stripeAchBankSelectionHandler.fetchSelectedBankId("clientSecret")
        }
    }

    @Test
    fun `handle() should return failure when onAcceptMandate() is called and completion delegate call fails`() = runTest {
        val dateSlot = slot<Date>()
        coEvery {
            stripeAchBankSelectionHandler.fetchSelectedBankId(any())
        } returns Result.success("paymentMethodId")
        coEvery {
            stripeAchMandateTimestampLoggingDelegate.logTimestamp(
                stripePaymentIntentId = any(),
                date = capture(dateSlot)
            )
        } just Runs
        val error = Exception()
        coEvery {
            completeStripeAchPaymentSessionDelegate.invoke(
                completeUrl = any(),
                paymentMethodId = any(),
                mandateTimestamp = any()
            )
        } returns Result.failure(error)
        val additionalInfoSlot = slot<AchAdditionalInfo.DisplayMandate>()
        coEvery { checkoutAdditionalInfoHandler.handle(capture(additionalInfoSlot)) } just Runs
        val payment = mockk<Payment>()
        every { paymentResultRepository.getPaymentResult().payment } returns payment

        val result = async {
            resolver.handle(
                clientSecret = "clientSecret",
                paymentIntentId = "paymentMethodId",
                sdkCompleteUrl = "sdkCompleteUrl"
            )
        }
        delay(1.seconds)
        val acceptJob = launch {
            additionalInfoSlot.captured.onAcceptMandate()
        }

        acceptJob.join()
        assertIs<Exception>(result.await().exceptionOrNull())
        coVerify {
            stripeAchBankSelectionHandler.fetchSelectedBankId("clientSecret")
            stripeAchMandateTimestampLoggingDelegate.logTimestamp(
                stripePaymentIntentId = "paymentMethodId",
                date = dateSlot.captured
            )
            completeStripeAchPaymentSessionDelegate(
                completeUrl = "sdkCompleteUrl",
                paymentMethodId = "paymentMethodId",
                mandateTimestamp = dateSlot.captured
            )
            checkoutAdditionalInfoHandler.handle(additionalInfoSlot.captured)
        }
        verify(exactly = 0) {
            paymentResultRepository.getPaymentResult().payment
        }
    }

    @Test
    fun `handle() should return failure when onDeclineMandate() is called`() = runTest {
        coEvery {
            stripeAchBankSelectionHandler.fetchSelectedBankId(any())
        } returns Result.success("paymentMethodId")
        val additionalInfoSlot = slot<AchAdditionalInfo.DisplayMandate>()
        coEvery { checkoutAdditionalInfoHandler.handle(capture(additionalInfoSlot)) } just Runs

        val result = async {
            resolver.handle(
                clientSecret = "clientSecret",
                paymentIntentId = "paymentMethodId",
                sdkCompleteUrl = "sdkCompleteUrl"
            )
        }
        delay(1.seconds)
        val acceptJob = launch {
            additionalInfoSlot.captured.onDeclineMandate()
        }

        acceptJob.join()
        assertEquals(
            PaymentMethodCancelledException(
                paymentMethodType = PaymentMethodType.STRIPE_ACH.name
            ),
            result.await().exceptionOrNull()
        )
        coVerify {
            stripeAchBankSelectionHandler.fetchSelectedBankId("clientSecret")
            checkoutAdditionalInfoHandler.handle(additionalInfoSlot.captured)
        }
    }
}
