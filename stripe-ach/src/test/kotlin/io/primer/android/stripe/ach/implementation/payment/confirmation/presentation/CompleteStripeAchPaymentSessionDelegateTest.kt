package io.primer.android.stripe.ach.implementation.payment.confirmation.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.primer.android.core.extensions.toIso8601String
import io.primer.android.stripe.ach.implementation.payment.confirmation.domain.StripeAchCompletePaymentInteractor
import io.primer.android.stripe.ach.implementation.payment.confirmation.domain.model.StripeAchCompletePaymentParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Calendar

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
class CompleteStripeAchPaymentSessionDelegateTest {
    @MockK
    private lateinit var stripeAchCompletePaymentInteractor: StripeAchCompletePaymentInteractor

    @InjectMockKs
    private lateinit var delegate: CompleteStripeAchPaymentSessionDelegate

    @AfterEach
    fun tearDown() {
        confirmVerified(stripeAchCompletePaymentInteractor)
    }

    @Test
    fun `invoke() should return success if interactor call succeeds and payment method id is not null`() = runTest {
        coEvery { stripeAchCompletePaymentInteractor.invoke(any()) } returns Result.success(Unit)

        val mandateTimestamp = Calendar.getInstance().apply {
            set(2024, 4, 10)
        }.time
        val result = delegate.invoke(
            completeUrl = "completionUrl",
            paymentMethodId = "paymentMethodId",
            mandateTimestamp = mandateTimestamp
        )

        assert(result.isSuccess)
        coVerify {
            stripeAchCompletePaymentInteractor.invoke(
                StripeAchCompletePaymentParams(
                    completeUrl = "completionUrl",
                    mandateTimestamp = mandateTimestamp.toIso8601String(),
                    paymentMethodId = "paymentMethodId"
                )
            )
        }
    }

    @Test
    fun `invoke() should return success if interactor call succeeds and payment method id is null`() = runTest {
        coEvery { stripeAchCompletePaymentInteractor.invoke(any()) } returns Result.success(Unit)

        val mandateTimestamp = Calendar.getInstance().apply {
            set(2024, 4, 10)
        }.time
        val result = delegate.invoke(
            completeUrl = "completionUrl",
            paymentMethodId = null,
            mandateTimestamp = mandateTimestamp
        )

        assert(result.isSuccess)
        coVerify {
            stripeAchCompletePaymentInteractor.invoke(
                StripeAchCompletePaymentParams(
                    completeUrl = "completionUrl",
                    mandateTimestamp = mandateTimestamp.toIso8601String(),
                    paymentMethodId = null
                )
            )
        }
    }

    @Test
    fun `invoke() should return failure if interactor call succeeds`() = runTest {
        coEvery {
            stripeAchCompletePaymentInteractor.invoke(any())
        } returns Result.failure(Exception())

        val mandateTimestamp = Calendar.getInstance().apply {
            set(2024, 4, 10)
        }.time
        val result = delegate.invoke(
            completeUrl = "completionUrl",
            paymentMethodId = "paymentMethodId",
            mandateTimestamp = mandateTimestamp
        )

        assert(result.isFailure)
        coVerify {
            stripeAchCompletePaymentInteractor.invoke(
                StripeAchCompletePaymentParams(
                    completeUrl = "completionUrl",
                    mandateTimestamp = mandateTimestamp.toIso8601String(),
                    paymentMethodId = "paymentMethodId"
                )
            )
        }
    }
}
