package io.primer.android.payments.core.status.domain

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.errors.data.exception.PaymentMethodCancelledException
import io.primer.android.payments.InstantExecutorExtension
import io.primer.android.payments.core.status.domain.model.AsyncStatus
import io.primer.android.payments.core.status.domain.model.AsyncStatusParams
import io.primer.android.payments.core.status.domain.repository.AsyncPaymentMethodStatusRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
class AsyncPaymentMethodPollingInteractorTest {

    @RelaxedMockK
    internal lateinit var paymentMethodStatusRepository: AsyncPaymentMethodStatusRepository

    private lateinit var interactor: DefaultAsyncPaymentMethodPollingInteractor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        interactor = DefaultAsyncPaymentMethodPollingInteractor(
            paymentMethodStatusRepository = paymentMethodStatusRepository
        )
    }

    @Test
    fun `getting status should return resume token event when getAsyncStatus was success`() {
        val asyncStatus = AsyncStatus(resumeToken = "test_resume_token")

        coEvery { paymentMethodStatusRepository.getAsyncStatus(any()) }.returns(
            flowOf(asyncStatus)
        )
        val url = "https://www.example.com"

        runTest {
            val status = interactor(
                AsyncStatusParams(
                    url = url,
                    paymentMethodType = ""
                )
            ).first()

            assertEquals("test_resume_token", status.resumeToken)
        }

        coVerify { paymentMethodStatusRepository.getAsyncStatus(url) }
    }

    @Test
    fun `getting status should return error event when getAsyncStatus failed`() {
        val exception = mockk<Exception>(relaxed = true)
        every { exception.message }.returns("Failed to get status.")

        coEvery { paymentMethodStatusRepository.getAsyncStatus(any()) }.returns(
            flow { throw exception }
        )
        val url = "https://www.example.com"
        assertThrows<Exception> {
            runTest {
                interactor(
                    AsyncStatusParams(
                        url,
                        ""
                    )
                ).first()
            }
        }

        coVerify { paymentMethodStatusRepository.getAsyncStatus(url) }
    }

    @Test
    fun `getting status should return error with PaymentMethodCancelledException when getAsyncStatus failed with CancellationException`() {
        val exception = mockk<CancellationException>(relaxed = true)
        every { exception.message }.returns("Job cancelled.")
        coEvery { paymentMethodStatusRepository.getAsyncStatus(any()) }.returns(
            flow { throw exception }
        )
        assertThrows<PaymentMethodCancelledException> {
            runTest {
                interactor(
                    AsyncStatusParams(
                        "",
                        "GOOGLE_PAY"
                    )
                ).first()
            }
        }

        coVerify { paymentMethodStatusRepository.getAsyncStatus(any()) }
    }
}
