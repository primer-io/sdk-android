package io.primer.android.stripe.ach.implementation.configuration.domain

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.stripe.ach.implementation.configuration.domain.model.StripeAchConfig
import io.primer.android.stripe.ach.implementation.configuration.domain.model.StripeAchConfigParams
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class DefaultStripeAchConfigurationInteractorTest {
    @MockK
    private lateinit var configurationRepository:
        PaymentMethodConfigurationRepository<StripeAchConfig, StripeAchConfigParams>

    @InjectMockKs
    private lateinit var interactor: DefaultStripeAchConfigurationInteractor

    @Test
    fun `invoke() returns success result when repository returns success`() =
        runBlocking {
            val params = mockk<StripeAchConfigParams>()
            val expectedConfig = mockk<StripeAchConfig>()
            val result = Result.success(expectedConfig)

            every { configurationRepository.getPaymentMethodConfiguration(params) } returns result

            val response = interactor.invoke(params)

            verify { configurationRepository.getPaymentMethodConfiguration(params) }
            assertEquals(result, response)
        }

    @Test
    fun `invoke() returns failure result when repository returns failure`() =
        runBlocking {
            val params = mockk<StripeAchConfigParams>()
            val exception = Exception("Failed to fetch configuration")
            val result = Result.failure<StripeAchConfig>(exception)

            every { configurationRepository.getPaymentMethodConfiguration(params) } returns result

            val response = interactor.invoke(params)

            verify { configurationRepository.getPaymentMethodConfiguration(params) }
            assertEquals(result, response)
        }
}
