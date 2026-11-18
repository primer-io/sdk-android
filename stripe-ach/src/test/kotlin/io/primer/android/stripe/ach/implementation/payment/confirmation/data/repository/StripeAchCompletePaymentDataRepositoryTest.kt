package io.primer.android.stripe.ach.implementation.payment.confirmation.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.primer.android.core.data.model.BaseRemoteUrlRequest
import io.primer.android.core.data.model.EmptyDataResponse
import io.primer.android.core.data.network.exception.InvalidUrlException
import io.primer.android.stripe.ach.implementation.payment.confirmation.data.datasource.RemoteStripeAchCompletePaymentDataSource
import io.primer.android.stripe.ach.implementation.payment.confirmation.data.model.StripeAchCompletePaymentDataRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
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
    fun `completePayment() should return success when data source call succeeds and payment method id is not null`() =
        runTest {
            coEvery { completePaymentDataSource.execute(any()) } returns EmptyDataResponse()

            val result =
                repository.completePayment(
                    COMPLETE_URL,
                    "mandateTimestamp",
                    "paymentMethodId",
                )

            assertEquals(
                Result.success(Unit),
                result,
            )
            coVerify {
                completePaymentDataSource.execute(
                    BaseRemoteUrlRequest(
                        url = COMPLETE_URL,
                        data =
                        StripeAchCompletePaymentDataRequest(
                            mandateTimestamp = "mandateTimestamp",
                            paymentMethodId = "paymentMethodId",
                        ),
                    ),
                )
            }
            confirmVerified(completePaymentDataSource)
        }

    @Test
    fun `completePayment() should return success when data source call succeeds and payment method id is null`() =
        runTest {
            coEvery { completePaymentDataSource.execute(any()) } returns EmptyDataResponse()

            val result =
                repository.completePayment(
                    completeUrl = COMPLETE_URL,
                    mandateTimestamp = "mandateTimestamp",
                    paymentMethodId = null,
                )

            assertEquals(
                Result.success(Unit),
                result,
            )
            coVerify {
                completePaymentDataSource.execute(
                    BaseRemoteUrlRequest(
                        url = COMPLETE_URL,
                        data =
                        StripeAchCompletePaymentDataRequest(
                            mandateTimestamp = "mandateTimestamp",
                            paymentMethodId = null,
                        ),
                    ),
                )
            }
            confirmVerified(completePaymentDataSource)
        }

    @Test
    fun `completePayment() should return failure when data source call fails`() =
        runTest {
            coEvery { completePaymentDataSource.execute(any()) } throws Exception()

            val result =
                repository.completePayment(
                    COMPLETE_URL,
                    "mandateTimestamp",
                    "paymentMethodId",
                )

            assert(result.isFailure)
            coVerify {
                completePaymentDataSource.execute(
                    BaseRemoteUrlRequest(
                        url = COMPLETE_URL,
                        data =
                        StripeAchCompletePaymentDataRequest(
                            mandateTimestamp = "mandateTimestamp",
                            paymentMethodId = "paymentMethodId",
                        ),
                    ),
                )
            }
            confirmVerified(completePaymentDataSource)
        }

    @Test
    fun `completePayment() should return failure without a request when the url is not a Primer url`() =
        runTest {
            val result =
                repository.completePayment(
                    "https://attacker.example/complete",
                    "mandateTimestamp",
                    "paymentMethodId",
                )

            assertInstanceOf(InvalidUrlException::class.java, result.exceptionOrNull())
            coVerify(exactly = 0) { completePaymentDataSource.execute(any()) }
        }

    private companion object {
        const val COMPLETE_URL = "https://api.sandbox.primer.io/complete"
    }
}
