package io.primer.android.paypal.implementation.tokenization.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.core.InstantExecutorExtension
import io.primer.android.core.toListDuring
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.paypal.implementation.configuration.domain.PaypalConfigurationInteractor
import io.primer.android.paypal.implementation.configuration.domain.model.PaypalConfig
import io.primer.android.paypal.implementation.configuration.domain.model.PaypalConfigParams
import io.primer.android.paypal.implementation.tokenization.domain.PaypalCreateBillingAgreementInteractor
import io.primer.android.paypal.implementation.tokenization.domain.PaypalCreateOrderInteractor
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalBillingAgreement
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalOrder
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodLauncherParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.time.Duration.Companion.seconds

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
internal class PaypalTokenizationCollectorDelegateTest {
    private val configurationInteractor: PaypalConfigurationInteractor = mockk()
    private val createOrderInteractor: PaypalCreateOrderInteractor = mockk()
    private val createBillingAgreementInteractor: PaypalCreateBillingAgreementInteractor = mockk()

    private lateinit var delegate: PaypalTokenizationCollectorDelegate

    @BeforeEach
    fun setUp() {
        delegate =
            PaypalTokenizationCollectorDelegate(
                configurationInteractor,
                createOrderInteractor,
                createBillingAgreementInteractor,
            )
    }

    @Test
    fun `startDataCollection should navigate to correct URL for PaypalCheckoutConfiguration`() =
        runTest {
            // Given
            val params = PaypalTokenizationCollectorParams(primerSessionIntent = mockk())
            val configuration =
                PaypalConfig.PaypalCheckoutConfiguration(
                    paymentMethodConfigId = "config-id",
                    amount = 100,
                    currencyCode = "USD",
                    successUrl = "https://success.url",
                    cancelUrl = "https://cancel.url",
                )
            val orderResponse =
                PaypalOrder(
                    orderId = "order-id",
                    approvalUrl = "https://approval.url",
                    successUrl = "https://success.url",
                    cancelUrl = "https://cancel.url",
                )

            coEvery { configurationInteractor(PaypalConfigParams(params.primerSessionIntent)) } returns
                Result.success(
                    configuration,
                )
            coEvery { createOrderInteractor(any()) } returns Result.success(orderResponse)

            // When
            launch {
                delegate.startDataCollection(params)
            }

            // Then
            val events = delegate.uiEvent.toListDuring(1.0.seconds)
            val navigateEvent = events.find { it is ComposerUiEvent.Navigate } as? ComposerUiEvent.Navigate
            assertTrue(navigateEvent != null)
            assertTrue(navigateEvent?.params is PaymentMethodLauncherParams)

            coVerify { configurationInteractor(PaypalConfigParams(params.primerSessionIntent)) }
            coVerify { createOrderInteractor(any()) }
        }

    @Test
    fun `startDataCollection should navigate to correct URL for PaypalVaultConfiguration`() =
        runTest {
            // Given
            val params = PaypalTokenizationCollectorParams(primerSessionIntent = mockk())
            val configuration =
                PaypalConfig.PaypalVaultConfiguration(
                    paymentMethodConfigId = "config-id",
                    successUrl = "https://success.url",
                    cancelUrl = "https://cancel.url",
                )
            val agreementResponse =
                PaypalBillingAgreement(
                    approvalUrl = "https://approval.url",
                    successUrl = "https://success.url",
                    paymentMethodConfigId = "config-id",
                    cancelUrl = "https://cancel.url",
                )

            coEvery {
                configurationInteractor(PaypalConfigParams(params.primerSessionIntent))
            } returns Result.success(configuration)
            coEvery { createBillingAgreementInteractor(any()) } returns Result.success(agreementResponse)

            // When
            launch {
                delegate.startDataCollection(params)
            }

            // Then
            val events = delegate.uiEvent.toListDuring(1.0.seconds)
            val navigateEvent = events.find { it is ComposerUiEvent.Navigate } as? ComposerUiEvent.Navigate
            assertTrue(navigateEvent != null)
            assertTrue(navigateEvent?.params is PaymentMethodLauncherParams)

            coVerify { configurationInteractor(PaypalConfigParams(params.primerSessionIntent)) }
            coVerify { createBillingAgreementInteractor(any()) }
        }

    @Test
    fun `startDataCollection should handle errors`() =
        runTest {
            // Given
            val params = PaypalTokenizationCollectorParams(primerSessionIntent = mockk())
            coEvery { configurationInteractor(PaypalConfigParams(params.primerSessionIntent)) } returns
                Result.failure(
                    Exception("Test exception"),
                )

            // When
            val result = delegate.startDataCollection(params)

            // Then
            coVerify { configurationInteractor(PaypalConfigParams(params.primerSessionIntent)) }
            assertTrue(result.isFailure)
        }
}
