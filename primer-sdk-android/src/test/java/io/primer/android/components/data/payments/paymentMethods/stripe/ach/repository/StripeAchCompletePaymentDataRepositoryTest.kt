package io.primer.android.components.data.payments.paymentMethods.stripe.ach.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.primer.android.components.data.payments.paymentMethods.stripe.ach.datasource.RemoteStripeAchCompletePaymentDataSource
import io.primer.android.components.data.payments.paymentMethods.stripe.ach.model.StripeAchCompletePaymentDataRequest
import io.primer.android.core.data.models.EmptyDataResponse
import io.primer.android.data.base.models.BaseRemoteUrlRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
class StripeAchCompletePaymentDataRepositoryTest {
    @MockK
    private lateinit var completePaymentDataSource: RemoteStripeAchCompletePaymentDataSource

    @InjectMockKs
    private lateinit var repository: StripeAchCompletePaymentDataRepository

    @Test
    fun `completePayment() should return success when data source call succeeds and payment method id is not null`() = runTest {
        coEvery { completePaymentDataSource.execute(any()) } returns EmptyDataResponse()

        val result = repository.completePayment(
            "completeUrl",
            "mandateTimestamp",
            "paymentMethodId"
        )

        assertEquals(
            Result.success(Unit),
            result
        )
        coVerify {
            completePaymentDataSource.execute(
                BaseRemoteUrlRequest(
                    url = "completeUrl",
                    data = StripeAchCompletePaymentDataRequest(
                        mandateTimestamp = "mandateTimestamp",
                        paymentMethodId = "paymentMethodId"
                    )
                )
            )
        }
        confirmVerified(completePaymentDataSource)
    }

    @Test
    fun `completePayment() should return success when data source call succeeds and payment method id is null`() = runTest {
        coEvery { completePaymentDataSource.execute(any()) } returns EmptyDataResponse()

        val result = repository.completePayment(
            completeUrl = "completeUrl",
            mandateTimestamp = "mandateTimestamp",
            paymentMethodId = null
        )

        assertEquals(
            Result.success(Unit),
            result
        )
        coVerify {
            completePaymentDataSource.execute(
                BaseRemoteUrlRequest(
                    url = "completeUrl",
                    data = StripeAchCompletePaymentDataRequest(
                        mandateTimestamp = "mandateTimestamp",
                        paymentMethodId = null
                    )
                )
            )
        }
        confirmVerified(completePaymentDataSource)
    }

    @Test
    fun `completePayment() should return failure when data source call fails`() = runTest {
        coEvery { completePaymentDataSource.execute(any()) } throws Exception()

        val result = repository.completePayment(
            "completeUrl",
            "mandateTimestamp",
            "paymentMethodId"
        )

        assert(result.isFailure)
        coVerify {
            completePaymentDataSource.execute(
                BaseRemoteUrlRequest(
                    url = "completeUrl",
                    data = StripeAchCompletePaymentDataRequest(
                        mandateTimestamp = "mandateTimestamp",
                        paymentMethodId = "paymentMethodId"
                    )
                )
            )
        }
        confirmVerified(completePaymentDataSource)
    }
}
