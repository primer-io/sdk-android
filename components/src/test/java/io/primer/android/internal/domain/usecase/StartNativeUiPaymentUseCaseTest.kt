package io.primer.android.internal.domain.usecase

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.internal.domain.repositories.HeadlessRepository
import io.primer.android.internal.domain.repositories.NativeUiRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StartNativeUiPaymentUseCaseTest {

    private lateinit var mockNativeUiRepository: NativeUiRepository
    private lateinit var mockHeadlessRepository: HeadlessRepository
    private lateinit var useCase: StartNativeUiPaymentUseCase

    @BeforeEach
    fun setup() {
        mockNativeUiRepository = mockk(relaxed = true)
        mockHeadlessRepository = mockk(relaxed = true)
        useCase = StartNativeUiPaymentUseCase(
            nativeUiRepository = mockNativeUiRepository,
            headlessRepository = mockHeadlessRepository,
        )
    }

    @Test
    fun `invoke starts payment flow and returns success result`() = runTest {
        val paymentMethodType = "GOOGLE_PAY"
        val expectedCheckoutData = mockk<PrimerCheckoutData>()
        val successResult = Result.success(expectedCheckoutData)

        justRun { mockNativeUiRepository.startPaymentFlow(paymentMethodType) }
        every { mockHeadlessRepository.paymentResults } returns flowOf(successResult)

        val result = useCase(paymentMethodType)

        assertTrue(result.isSuccess)
        assertEquals(expectedCheckoutData, result.getOrNull())
        verify(exactly = 1) { mockNativeUiRepository.startPaymentFlow(paymentMethodType) }
    }

    @Test
    fun `invoke starts payment flow and returns failure result`() = runTest {
        val paymentMethodType = "PAYPAL"
        val expectedError = RuntimeException("Payment failed")
        val failureResult = Result.failure<PrimerCheckoutData>(expectedError)

        justRun { mockNativeUiRepository.startPaymentFlow(paymentMethodType) }
        every { mockHeadlessRepository.paymentResults } returns flowOf(failureResult)

        val result = useCase(paymentMethodType)

        assertTrue(result.isFailure)
        assertEquals(expectedError, result.exceptionOrNull())
        verify(exactly = 1) { mockNativeUiRepository.startPaymentFlow(paymentMethodType) }
    }

    @Test
    fun `invoke waits for first payment result from flow`() = runTest {
        val paymentMethodType = "STRIPE"
        val expectedCheckoutData = mockk<PrimerCheckoutData>()
        val paymentResultsFlow = MutableSharedFlow<Result<PrimerCheckoutData>>(replay = 1)

        justRun { mockNativeUiRepository.startPaymentFlow(paymentMethodType) }
        every { mockHeadlessRepository.paymentResults } returns paymentResultsFlow

        paymentResultsFlow.emit(Result.success(expectedCheckoutData))

        val result = useCase(paymentMethodType)

        assertTrue(result.isSuccess)
        assertEquals(expectedCheckoutData, result.getOrNull())
    }

    @Test
    fun `cleanup delegates to native UI repository`() {
        justRun { mockNativeUiRepository.cleanup() }

        useCase.cleanup()

        verify(exactly = 1) { mockNativeUiRepository.cleanup() }
    }
}
