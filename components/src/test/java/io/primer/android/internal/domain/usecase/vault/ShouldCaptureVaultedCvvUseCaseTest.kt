package io.primer.android.internal.domain.usecase.vault

import io.mockk.every
import io.mockk.mockk
import io.primer.android.configuration.data.model.PaymentMethodRemoteConfigOptions
import io.primer.android.configuration.domain.CachePolicy
import io.primer.android.configuration.domain.model.Configuration
import io.primer.android.configuration.domain.model.ConfigurationParams
import io.primer.android.configuration.domain.model.PaymentMethodConfig
import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.data.tokenization.models.PaymentInstrumentData
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShouldCaptureVaultedCvvUseCaseTest {

    private lateinit var configurationInteractor: TestConfigurationInteractor
    private lateinit var useCase: ShouldCaptureVaultedCvvUseCase

    @BeforeEach
    fun setUp() {
        configurationInteractor = TestConfigurationInteractor()
        useCase = ShouldCaptureVaultedCvvUseCase(configurationInteractor)
    }

    @Test
    fun `invoke should return false when payment method is not a card`() = runTest {
        // Given
        val configuration = configurationWithPaymentMethods(emptyList())
        configurationInteractor.result = Result.success(configuration)

        val vaultedPaymentMethod = createVaultedPaymentMethod(PaymentMethodType.PAYPAL)

        // When
        val result = useCase(vaultedPaymentMethod)

        // Then
        assertTrue(result.isSuccess)
        assertFalse(result.getOrNull()!!)
        assertEquals(CachePolicy.ForceCache, configurationInteractor.capturedParams?.cachePolicy)
    }

    @Test
    fun `invoke should return true when captureVaultedCardCvv is enabled`() = runTest {
        // Given
        val configuration = configurationWithPaymentMethods(
            listOf(
                createPaymentMethodConfig(captureVaultedCardCvv = true),
            ),
        )
        configurationInteractor.result = Result.success(configuration)

        val vaultedPaymentMethod = createVaultedPaymentMethod(PaymentMethodType.PAYMENT_CARD)

        // When
        val result = useCase(vaultedPaymentMethod)

        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!)
    }

    @Test
    fun `invoke should return false when captureVaultedCardCvv is disabled`() = runTest {
        // Given
        val configuration = configurationWithPaymentMethods(
            listOf(
                createPaymentMethodConfig(captureVaultedCardCvv = false),
            ),
        )
        configurationInteractor.result = Result.success(configuration)

        val vaultedPaymentMethod = createVaultedPaymentMethod(PaymentMethodType.PAYMENT_CARD)

        // When
        val result = useCase(vaultedPaymentMethod)

        // Then
        assertTrue(result.isSuccess)
        assertFalse(result.getOrNull()!!)
    }

    @Test
    fun `invoke should propagate failure when configuration fetch fails`() = runTest {
        // Given
        val expected = IllegalStateException("Configuration failure")
        configurationInteractor.result = Result.failure(expected)

        val vaultedPaymentMethod = createVaultedPaymentMethod(PaymentMethodType.PAYMENT_CARD)

        // When
        val result = useCase(vaultedPaymentMethod)

        // Then
        assertTrue(result.isFailure)
        assertSame(expected, result.exceptionOrNull())
    }

    private fun createVaultedPaymentMethod(type: PaymentMethodType) =
        PrimerVaultedPaymentMethod(
            id = "vault-id",
            analyticsId = "analytics-id",
            paymentInstrumentType = type.name,
            paymentMethodType = type.name,
            paymentInstrumentData = PaymentInstrumentData(),
        )

    private fun createPaymentMethodConfig(captureVaultedCardCvv: Boolean?) =
        PaymentMethodConfig(
            id = "payment-method-id",
            name = "Card",
            type = PaymentMethodType.PAYMENT_CARD.name,
            options = PaymentMethodRemoteConfigOptions(
                merchantId = null,
                merchantAccountId = null,
                merchantAppId = null,
                threeDSecureEnabled = null,
                extraMerchantData = null,
                captureVaultedCardCvv = captureVaultedCardCvv,
            ),
        )

    private fun configurationWithPaymentMethods(
        paymentMethods: List<PaymentMethodConfig>,
    ): Configuration {
        val configuration = mockk<Configuration>()
        every { configuration.paymentMethods } returns paymentMethods
        return configuration
    }

    private class TestConfigurationInteractor : BaseSuspendInteractor<Configuration, ConfigurationParams>() {
        var result: Result<Configuration> = Result.failure(IllegalStateException("Result not set"))
        var capturedParams: ConfigurationParams? = null

        override val dispatcher = Dispatchers.Unconfined

        override suspend fun performAction(params: ConfigurationParams): Result<Configuration> {
            capturedParams = params
            return result
        }
    }
}
