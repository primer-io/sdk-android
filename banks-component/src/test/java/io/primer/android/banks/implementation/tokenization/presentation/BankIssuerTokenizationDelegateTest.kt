package io.primer.android.banks.implementation.tokenization.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.PrimerSessionIntent
import io.primer.android.analytics.data.models.SdkIntegrationType
import io.primer.android.banks.implementation.configuration.domain.BankIssuerConfigurationInteractor
import io.primer.android.banks.implementation.configuration.domain.model.BankIssuerConfig
import io.primer.android.banks.implementation.configuration.domain.model.BankIssuerConfigParams
import io.primer.android.banks.implementation.tokenization.domain.BankIssuerTokenizationInteractor
import io.primer.android.banks.implementation.tokenization.domain.model.BankIssuerPaymentInstrumentParams
import io.primer.android.banks.implementation.tokenization.presentation.model.BankIssuerTokenizationInputable
import io.primer.android.clientSessionActions.domain.ActionInteractor
import io.primer.android.clientSessionActions.domain.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.clientSessionActions.domain.models.MultipleActionUpdateParams
import io.primer.android.configuration.domain.model.ClientSessionData
import io.primer.android.core.domain.None
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.payments.core.tokenization.data.model.PaymentMethodTokenInternal
import io.primer.android.payments.core.tokenization.data.model.toPaymentMethodToken
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.webRedirectShared.implementation.deeplink.domain.RedirectDeeplinkInteractor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantExecutorExtension::class)
class BankIssuerTokenizationDelegateTest {
    private lateinit var configurationInteractor: BankIssuerConfigurationInteractor
    private lateinit var tokenizationInteractor: BankIssuerTokenizationInteractor
    private lateinit var deeplinkInteractor: RedirectDeeplinkInteractor
    private lateinit var actionInteractor: ActionInteractor
    private lateinit var delegate: BankIssuerTokenizationDelegate
    private lateinit var primerSettings: PrimerSettings

    private val input =
        BankIssuerTokenizationInputable(
            paymentMethodType = "BankIssuer",
            primerSessionIntent = PrimerSessionIntent.CHECKOUT,
            bankIssuer = "bankIssuer",
        )
    private val bankIssuerConfigParams = BankIssuerConfigParams(paymentMethodType = input.paymentMethodType)
    private val bankIssuerConfig =
        BankIssuerConfig(
            paymentMethodConfigId = "BankIssuer",
            locale = Locale.US,
        )

    @BeforeEach
    fun setUp() {
        configurationInteractor = mockk()
        tokenizationInteractor = mockk()
        deeplinkInteractor = mockk()
        actionInteractor = mockk(relaxed = true)
        primerSettings = mockk(relaxed = true)
        delegate =
            BankIssuerTokenizationDelegate(
                configurationInteractor = configurationInteractor,
                tokenizationInteractor = tokenizationInteractor,
                primerSettings = primerSettings,
                actionInteractor = actionInteractor,
                deeplinkInteractor = deeplinkInteractor,
            )
    }

    @Test
    fun `invoke() should perform tokenization via interactor, select payment method and return success when interactor calls succeed and integration type is headless`() =
        runTest {
            every { deeplinkInteractor(None) } returns "https://example.com"
            every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
            mockkStatic("io.primer.android.payments.core.tokenization.data.model.PaymentMethodTokenInternalKt")
            coEvery { actionInteractor(any()) } returns Result.success(mockk())
            coEvery { configurationInteractor(any()) } returns
                Result.success(
                    mockk<BankIssuerConfig> {
                        every { this@mockk.paymentMethodConfigId } returns "config_id"
                        every { this@mockk.locale.toLanguageTag() } returns "language_tag"
                    },
                )
            val paymentMethodTokenInternal = mockk<PaymentMethodTokenInternal>()
            coEvery { tokenizationInteractor.invoke(any()) } returns Result.success(paymentMethodTokenInternal)
            val paymentMethodTokenData = mockk<PrimerPaymentMethodTokenData>()
            every { paymentMethodTokenInternal.toPaymentMethodToken() } returns paymentMethodTokenData

            val result =
                delegate.tokenize(
                    BankIssuerTokenizationInputable(
                        paymentMethodType = PaymentMethodType.ADYEN_IDEAL.name,
                        primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                        bankIssuer = "bank_issuer",
                    ),
                )

            kotlin.test.assertEquals(paymentMethodTokenData, result.getOrNull())
            coVerify {
                actionInteractor(
                    MultipleActionUpdateParams(
                        params =
                            listOf(
                                ActionUpdateSelectPaymentMethodParams(
                                    paymentMethodType = PaymentMethodType.ADYEN_IDEAL.name,
                                    cardNetwork = null,
                                ),
                            ),
                    ),
                )
                configurationInteractor.invoke(BankIssuerConfigParams(PaymentMethodType.ADYEN_IDEAL.name))
                tokenizationInteractor(
                    TokenizationParams(
                        paymentInstrumentParams =
                            BankIssuerPaymentInstrumentParams(
                                paymentMethodType = PaymentMethodType.ADYEN_IDEAL.name,
                                paymentMethodConfigId = "config_id",
                                locale = "language_tag",
                                redirectionUrl = "https://example.com",
                                bankIssuer = "bank_issuer",
                            ),
                        sessionIntent = PrimerSessionIntent.CHECKOUT,
                    ),
                )
            }
            verify {
                paymentMethodTokenInternal.toPaymentMethodToken()
                primerSettings.sdkIntegrationType
            }
            unmockkStatic("io.primer.android.payments.core.tokenization.data.model.PaymentMethodTokenInternalKt")
        }

    @Test
    fun `invoke() should perform tokenization via interactor, not select payment method and return success when interactor calls succeed and integration type is drop-in`() =
        runTest {
            every { deeplinkInteractor(None) } returns "https://example.com"
            every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.DROP_IN
            mockkStatic("io.primer.android.payments.core.tokenization.data.model.PaymentMethodTokenInternalKt")
            coEvery { configurationInteractor(any()) } returns
                Result.success(
                    mockk<BankIssuerConfig> {
                        every { this@mockk.paymentMethodConfigId } returns "config_id"
                        every { this@mockk.locale.toLanguageTag() } returns "language_tag"
                    },
                )
            val paymentMethodTokenInternal = mockk<PaymentMethodTokenInternal>()
            coEvery { tokenizationInteractor.invoke(any()) } returns Result.success(paymentMethodTokenInternal)
            val paymentMethodTokenData = mockk<PrimerPaymentMethodTokenData>()
            every { paymentMethodTokenInternal.toPaymentMethodToken() } returns paymentMethodTokenData

            val result =
                delegate.tokenize(
                    BankIssuerTokenizationInputable(
                        paymentMethodType = PaymentMethodType.ADYEN_IDEAL.name,
                        primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                        bankIssuer = "bank_issuer",
                    ),
                )

            kotlin.test.assertEquals(paymentMethodTokenData, result.getOrNull())
            coVerify {
                configurationInteractor.invoke(BankIssuerConfigParams(PaymentMethodType.ADYEN_IDEAL.name))
                tokenizationInteractor(
                    TokenizationParams(
                        paymentInstrumentParams =
                            BankIssuerPaymentInstrumentParams(
                                paymentMethodType = PaymentMethodType.ADYEN_IDEAL.name,
                                paymentMethodConfigId = "config_id",
                                locale = "language_tag",
                                redirectionUrl = "https://example.com",
                                bankIssuer = "bank_issuer",
                            ),
                        sessionIntent = PrimerSessionIntent.CHECKOUT,
                    ),
                )
            }
            coVerify(exactly = 0) {
                actionInteractor(any())
            }
            verify {
                paymentMethodTokenInternal.toPaymentMethodToken()
                primerSettings.sdkIntegrationType
            }
            unmockkStatic("io.primer.android.payments.core.tokenization.data.model.PaymentMethodTokenInternalKt")
        }

    @Test
    fun `mapTokenizationData returns success result when the configurationInteractor returns Result success`() =
        runTest {
            every { deeplinkInteractor(None) } returns "https://example.com"
            coEvery { configurationInteractor(bankIssuerConfigParams) } returns Result.success(bankIssuerConfig)
            val clientSessionData = mockk<ClientSessionData>(relaxed = true)
            coEvery { actionInteractor(any()) } returns Result.success(clientSessionData)

            val result = delegate.mapTokenizationData(input)

            assertTrue(result.isSuccess)
            val tokenizationParams = result.getOrNull()!!
            val paymentInstrumentParams = tokenizationParams.paymentInstrumentParams

            assertEquals(input.paymentMethodType, paymentInstrumentParams.paymentMethodType)
            coVerify { configurationInteractor(bankIssuerConfigParams) }
        }

    @Test
    fun `mapTokenizationData returns failure result when the configurationInteractor returns Result failure`() =
        runTest {
            val exception = Exception("Configuration error")
            coEvery { configurationInteractor(bankIssuerConfigParams) } returns Result.failure(exception)
            val clientSessionData = mockk<ClientSessionData>(relaxed = true)
            coEvery { actionInteractor(any()) } returns Result.success(clientSessionData)

            val result = delegate.mapTokenizationData(input)

            assertTrue(result.isFailure)
            assertEquals(exception, result.exceptionOrNull())

            coVerify { configurationInteractor(bankIssuerConfigParams) }
        }
}
