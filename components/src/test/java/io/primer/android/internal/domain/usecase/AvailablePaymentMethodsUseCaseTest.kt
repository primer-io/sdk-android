package io.primer.android.internal.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.configuration.domain.model.Surcharge
import io.primer.android.core.domain.None
import io.primer.android.internal.data.mappers.PaymentMethodMapper
import io.primer.android.internal.domain.models.PrimerComposablePaymentMethod
import io.primer.android.internal.domain.repositories.HeadlessRepository
import io.primer.android.ui.core.payment.domain.interactor.SurchargeInteractor
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AvailablePaymentMethodsUseCaseTest {

    private lateinit var useCase: AvailablePaymentMethodsUseCase
    private lateinit var mockHeadlessRepository: HeadlessRepository
    private lateinit var mockPaymentMethodMapper: PaymentMethodMapper
    private lateinit var mockSurchargeInteractor: SurchargeInteractor

    @BeforeEach
    fun setUp() {
        mockHeadlessRepository = mockk()
        mockPaymentMethodMapper = mockk()
        mockSurchargeInteractor = mockk()

        useCase = AvailablePaymentMethodsUseCase(
            headlessRepository = mockHeadlessRepository,
            paymentMethodMapper = mockPaymentMethodMapper,
            surchargeInteractor = mockSurchargeInteractor,
        )
    }

    @Test
    fun `invoke should return success with mapped payment methods and surcharges`() = runTest {
        // Given
        val headlessPaymentMethod1 = mockk<PrimerHeadlessUniversalCheckoutPaymentMethod> {
            every { paymentMethodType } returns "PAYMENT_CARD"
        }
        val headlessPaymentMethod2 = mockk<PrimerHeadlessUniversalCheckoutPaymentMethod> {
            every { paymentMethodType } returns "GOOGLE_PAY"
        }
        val rawPaymentMethods = listOf(headlessPaymentMethod1, headlessPaymentMethod2)

        val surcharge = mockk<Surcharge>()
        val surcharges = mapOf("PAYMENT_CARD" to surcharge)

        val composablePaymentMethod1 = mockk<PrimerComposablePaymentMethod>()
        val composablePaymentMethod2 = mockk<PrimerComposablePaymentMethod>()

        coEvery { mockHeadlessRepository.getAvailablePaymentMethods() } returns rawPaymentMethods
        coEvery { mockSurchargeInteractor.execute(None) } returns surcharges
        every {
            mockPaymentMethodMapper.toComposable(headlessPaymentMethod1, surcharges)
        } returns composablePaymentMethod1
        every {
            mockPaymentMethodMapper.toComposable(headlessPaymentMethod2, surcharges)
        } returns composablePaymentMethod2

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(listOf(composablePaymentMethod1, composablePaymentMethod2), useCase.cache)

        coVerify(exactly = 1) { mockHeadlessRepository.getAvailablePaymentMethods() }
        coVerify(exactly = 1) { mockSurchargeInteractor.execute(None) }
        coVerify(exactly = 1) { mockPaymentMethodMapper.toComposable(headlessPaymentMethod1, surcharges) }
        coVerify(exactly = 1) { mockPaymentMethodMapper.toComposable(headlessPaymentMethod2, surcharges) }
    }

    @Test
    fun `invoke should handle surcharge interactor exception and use empty map`() = runTest {
        // Given
        val headlessPaymentMethod = mockk<PrimerHeadlessUniversalCheckoutPaymentMethod>()
        val rawPaymentMethods = listOf(headlessPaymentMethod)
        val composablePaymentMethod = mockk<PrimerComposablePaymentMethod>()

        coEvery { mockHeadlessRepository.getAvailablePaymentMethods() } returns rawPaymentMethods
        coEvery { mockSurchargeInteractor.execute(None) } throws Exception("Surcharge error")
        every {
            mockPaymentMethodMapper.toComposable(headlessPaymentMethod, emptyMap())
        } returns composablePaymentMethod

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(listOf(composablePaymentMethod), useCase.cache)

        coVerify(exactly = 1) { mockHeadlessRepository.getAvailablePaymentMethods() }
        coVerify(exactly = 1) { mockSurchargeInteractor.execute(None) }
        coVerify(exactly = 1) { mockPaymentMethodMapper.toComposable(headlessPaymentMethod, emptyMap()) }
    }

    @Test
    fun `invoke should return failure when repository throws exception`() = runTest {
        // Given
        val exception = Exception("Repository error")
        coEvery { mockHeadlessRepository.getAvailablePaymentMethods() } throws exception

        // When
        val result = useCase()

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception.message, result.exceptionOrNull()?.message)
        assertTrue(useCase.cache.isEmpty())

        coVerify(exactly = 1) { mockHeadlessRepository.getAvailablePaymentMethods() }
        coVerify(exactly = 0) { mockSurchargeInteractor.execute(any()) }
    }

    @Test
    fun `invoke should handle empty payment methods list`() = runTest {
        // Given
        val rawPaymentMethods = emptyList<PrimerHeadlessUniversalCheckoutPaymentMethod>()
        val surcharges = mapOf("PAYMENT_CARD" to mockk<Surcharge>())

        coEvery { mockHeadlessRepository.getAvailablePaymentMethods() } returns rawPaymentMethods
        coEvery { mockSurchargeInteractor.execute(None) } returns surcharges

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        assertTrue(useCase.cache.isEmpty())

        coVerify(exactly = 1) { mockHeadlessRepository.getAvailablePaymentMethods() }
        coVerify(exactly = 1) { mockSurchargeInteractor.execute(None) }
    }

    @Test
    fun `cache should be initially empty`() {
        // Then
        assertTrue(useCase.cache.isEmpty())
    }
}
