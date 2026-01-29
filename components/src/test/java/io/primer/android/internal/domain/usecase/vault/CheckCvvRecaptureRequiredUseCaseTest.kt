package io.primer.android.internal.domain.usecase.vault

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.primer.android.configuration.data.model.PaymentMethodRemoteConfigOptions
import io.primer.android.configuration.domain.CachePolicy
import io.primer.android.configuration.domain.ConfigurationInteractor
import io.primer.android.configuration.domain.model.Configuration
import io.primer.android.configuration.domain.model.ConfigurationParams
import io.primer.android.configuration.domain.model.PaymentMethodConfig
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CheckCvvRecaptureRequiredUseCaseTest {

    private lateinit var configurationInteractor: ConfigurationInteractor
    private lateinit var useCase: CheckCvvRecaptureRequiredUseCase

    @BeforeEach
    fun setUp() {
        configurationInteractor = mockk()
        useCase = CheckCvvRecaptureRequiredUseCase(configurationInteractor)
    }

    @Test
    fun `invoke should return true when captureVaultedCardCvv is true for matching payment method`() = runTest {
        val paymentMethodId = "pm_123"
        val paymentMethod = mockk<PrimerVaultedPaymentMethod> {
            every { id } returns paymentMethodId
        }
        val options = mockk<PaymentMethodRemoteConfigOptions> {
            every { captureVaultedCardCvv } returns true
        }
        val paymentMethodConfig = PaymentMethodConfig(
            id = paymentMethodId,
            name = "Card",
            type = PaymentMethodType.PAYMENT_CARD.name,
            options = options,
        )
        val configuration = mockk<Configuration> {
            every { paymentMethods } returns listOf(paymentMethodConfig)
        }
        coEvery { configurationInteractor(any()) } returns Result.success(configuration)

        val result = useCase(paymentMethod)

        assertTrue(result)
        coVerify(exactly = 1) {
            configurationInteractor(
                ConfigurationParams(CachePolicy.ForceCache),
            )
        }
    }

    @Test
    fun `invoke should return false when captureVaultedCardCvv is false`() = runTest {
        val paymentMethodId = "pm_123"
        val paymentMethod = mockk<PrimerVaultedPaymentMethod> {
            every { id } returns paymentMethodId
        }
        val options = mockk<PaymentMethodRemoteConfigOptions> {
            every { captureVaultedCardCvv } returns false
        }
        val paymentMethodConfig = PaymentMethodConfig(
            id = paymentMethodId,
            name = "Card",
            type = PaymentMethodType.PAYMENT_CARD.name,
            options = options,
        )
        val configuration = mockk<Configuration> {
            every { paymentMethods } returns listOf(paymentMethodConfig)
        }
        coEvery { configurationInteractor(any()) } returns Result.success(configuration)

        val result = useCase(paymentMethod)

        assertFalse(result)
    }

    @Test
    fun `invoke should return false when captureVaultedCardCvv is null`() = runTest {
        val paymentMethodId = "pm_123"
        val paymentMethod = mockk<PrimerVaultedPaymentMethod> {
            every { id } returns paymentMethodId
        }
        val options = mockk<PaymentMethodRemoteConfigOptions> {
            every { captureVaultedCardCvv } returns null
        }
        val paymentMethodConfig = PaymentMethodConfig(
            id = paymentMethodId,
            name = "Card",
            type = PaymentMethodType.PAYMENT_CARD.name,
            options = options,
        )
        val configuration = mockk<Configuration> {
            every { paymentMethods } returns listOf(paymentMethodConfig)
        }
        coEvery { configurationInteractor(any()) } returns Result.success(configuration)

        val result = useCase(paymentMethod)

        assertFalse(result)
    }

    @Test
    fun `invoke should return false when PAYMENT_CARD config is not found`() = runTest {
        val paymentMethodId = "pm_123"
        val paymentMethod = mockk<PrimerVaultedPaymentMethod> {
            every { id } returns paymentMethodId
        }
        val options = mockk<PaymentMethodRemoteConfigOptions> {
            every { captureVaultedCardCvv } returns true
        }
        val paymentMethodConfig = PaymentMethodConfig(
            id = "different_id",
            name = "Other Payment Method",
            type = PaymentMethodType.GOOGLE_PAY.name,
            options = options,
        )
        val configuration = mockk<Configuration> {
            every { paymentMethods } returns listOf(paymentMethodConfig)
        }
        coEvery { configurationInteractor(any()) } returns Result.success(configuration)

        val result = useCase(paymentMethod)

        assertFalse(result)
    }

    @Test
    fun `invoke should return false when payment method type is not PAYMENT_CARD`() = runTest {
        val paymentMethodId = "pm_123"
        val paymentMethod = mockk<PrimerVaultedPaymentMethod> {
            every { id } returns paymentMethodId
        }
        val options = mockk<PaymentMethodRemoteConfigOptions> {
            every { captureVaultedCardCvv } returns true
        }
        val paymentMethodConfig = PaymentMethodConfig(
            id = paymentMethodId,
            name = "PayPal",
            type = PaymentMethodType.PAYPAL.name,
            options = options,
        )
        val configuration = mockk<Configuration> {
            every { paymentMethods } returns listOf(paymentMethodConfig)
        }
        coEvery { configurationInteractor(any()) } returns Result.success(configuration)

        val result = useCase(paymentMethod)

        assertFalse(result)
    }

    @Test
    fun `invoke should return false when options is null`() = runTest {
        val paymentMethodId = "pm_123"
        val paymentMethod = mockk<PrimerVaultedPaymentMethod> {
            every { id } returns paymentMethodId
        }
        val paymentMethodConfig = PaymentMethodConfig(
            id = paymentMethodId,
            name = "Card",
            type = PaymentMethodType.PAYMENT_CARD.name,
            options = null,
        )
        val configuration = mockk<Configuration> {
            every { paymentMethods } returns listOf(paymentMethodConfig)
        }
        coEvery { configurationInteractor(any()) } returns Result.success(configuration)

        val result = useCase(paymentMethod)

        assertFalse(result)
    }

    @Test
    fun `invoke should return false when configuration interactor fails`() = runTest {
        val paymentMethod = mockk<PrimerVaultedPaymentMethod> {
            every { id } returns "pm_123"
        }
        coEvery { configurationInteractor(any()) } returns Result.failure(RuntimeException("Config error"))

        val result = useCase(paymentMethod)

        assertFalse(result)
    }

    @Test
    fun `invoke should return false when payment methods list is empty`() = runTest {
        val paymentMethod = mockk<PrimerVaultedPaymentMethod> {
            every { id } returns "pm_123"
        }
        val configuration = mockk<Configuration> {
            every { paymentMethods } returns emptyList()
        }
        coEvery { configurationInteractor(any()) } returns Result.success(configuration)

        val result = useCase(paymentMethod)

        assertFalse(result)
    }
}
