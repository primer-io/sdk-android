package io.primer.android.stripe.ach.implementation.tokenization.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import io.primer.android.PrimerSessionIntent
import io.primer.android.analytics.data.models.SdkIntegrationType
import io.primer.android.clientSessionActions.domain.ActionInteractor
import io.primer.android.clientSessionActions.domain.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.clientSessionActions.domain.models.MultipleActionUpdateParams
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.payments.core.tokenization.data.model.PaymentMethodTokenInternal
import io.primer.android.payments.core.tokenization.data.model.toPaymentMethodToken
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.stripe.ach.implementation.configuration.domain.StripeAchConfigurationInteractor
import io.primer.android.stripe.ach.implementation.configuration.domain.model.StripeAchConfig
import io.primer.android.stripe.ach.implementation.configuration.domain.model.StripeAchConfigParams
import io.primer.android.stripe.ach.implementation.tokenization.domain.StripeAchTokenizationInteractor
import io.primer.android.stripe.ach.implementation.tokenization.domain.model.StripeAchPaymentInstrumentParams
import io.primer.android.stripe.ach.implementation.tokenization.presentation.model.StripeAchTokenizationInputable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
class StripeAchTokenizationDelegateTest {
    @MockK
    private lateinit var configurationInteractor: StripeAchConfigurationInteractor

    @MockK
    private lateinit var primerSettings: PrimerSettings

    @MockK
    private lateinit var tokenizationInteractor: StripeAchTokenizationInteractor

    @MockK
    private lateinit var actionInteractor: ActionInteractor

    @InjectMockKs
    private lateinit var delegate: StripeAchTokenizationDelegate

    @BeforeEach
    fun setUp() {
        confirmVerified(configurationInteractor, primerSettings, tokenizationInteractor, actionInteractor)
    }

    @Test
    fun `invoke() should perform tokenization via interactor, select payment method and return success when interactor calls succeed and integration type is headless`() =
        runTest {
            every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
            mockkStatic("io.primer.android.payments.core.tokenization.data.model.PaymentMethodTokenInternalKt")
            coEvery { actionInteractor(any()) } returns Result.success(mockk())
            coEvery { configurationInteractor(any()) } returns
                Result.success(
                    mockk<StripeAchConfig> {
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
                    StripeAchTokenizationInputable(
                        paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                        primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                    ),
                )

            assertEquals(paymentMethodTokenData, result.getOrNull())
            coVerify {
                actionInteractor(
                    MultipleActionUpdateParams(
                        params =
                            listOf(
                                ActionUpdateSelectPaymentMethodParams(
                                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                                    cardNetwork = null,
                                ),
                            ),
                    ),
                )
                configurationInteractor.invoke(StripeAchConfigParams(PaymentMethodType.STRIPE_ACH.name))
                tokenizationInteractor(
                    TokenizationParams(
                        paymentInstrumentParams =
                            StripeAchPaymentInstrumentParams(
                                paymentMethodConfigId = "config_id",
                                locale = "language_tag",
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
            every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.DROP_IN
            mockkStatic("io.primer.android.payments.core.tokenization.data.model.PaymentMethodTokenInternalKt")
            coEvery { configurationInteractor(any()) } returns
                Result.success(
                    mockk<StripeAchConfig> {
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
                    StripeAchTokenizationInputable(
                        paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                        primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                    ),
                )

            assertEquals(paymentMethodTokenData, result.getOrNull())
            coVerify {
                configurationInteractor.invoke(StripeAchConfigParams(PaymentMethodType.STRIPE_ACH.name))
                tokenizationInteractor(
                    TokenizationParams(
                        paymentInstrumentParams =
                            StripeAchPaymentInstrumentParams(
                                paymentMethodConfigId = "config_id",
                                locale = "language_tag",
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
    fun `invoke() should return failure when the action interactor call fails`() =
        runTest {
            val exception = Exception()
            every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
            coEvery { actionInteractor(any()) } returns Result.failure(exception)

            val result =
                delegate.tokenize(
                    StripeAchTokenizationInputable(
                        paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                        primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                    ),
                ).exceptionOrNull()

            assertEquals(exception, result)
            coVerify {
                actionInteractor(
                    MultipleActionUpdateParams(
                        params =
                            listOf(
                                ActionUpdateSelectPaymentMethodParams(
                                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                                    cardNetwork = null,
                                ),
                            ),
                    ),
                )
            }
            verify {
                primerSettings.sdkIntegrationType
            }
        }

    @Test
    fun `invoke() should return failure when the tokenization interactor call fails`() =
        runTest {
            every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
            coEvery { actionInteractor(any()) } returns Result.success(mockk())
            coEvery { configurationInteractor(any()) } returns
                Result.success(
                    mockk<StripeAchConfig> {
                        every { this@mockk.paymentMethodConfigId } returns "config_id"
                        every { this@mockk.locale.toLanguageTag() } returns "language_tag"
                    },
                )
            val exception = Exception()
            coEvery { tokenizationInteractor.invoke(any()) } returns Result.failure(exception)

            val result =
                delegate.tokenize(
                    StripeAchTokenizationInputable(
                        paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                        primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                    ),
                )

            assertEquals(exception, result.exceptionOrNull())
            coVerify {
                actionInteractor(
                    MultipleActionUpdateParams(
                        params =
                            listOf(
                                ActionUpdateSelectPaymentMethodParams(
                                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                                    cardNetwork = null,
                                ),
                            ),
                    ),
                )
                configurationInteractor.invoke(StripeAchConfigParams(PaymentMethodType.STRIPE_ACH.name))
                tokenizationInteractor(
                    TokenizationParams(
                        paymentInstrumentParams =
                            StripeAchPaymentInstrumentParams(
                                paymentMethodConfigId = "config_id",
                                locale = "language_tag",
                            ),
                        sessionIntent = PrimerSessionIntent.CHECKOUT,
                    ),
                )
            }
            verify {
                primerSettings.sdkIntegrationType
            }
        }

    @Test
    fun `invoke() should return failure when the configuration interactor call fails`() =
        runTest {
            every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
            coEvery { actionInteractor(any()) } returns Result.success(mockk())
            val exception = Exception()
            coEvery { configurationInteractor(any()) } returns Result.failure(exception)

            val result =
                delegate.tokenize(
                    StripeAchTokenizationInputable(
                        paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                        primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                    ),
                )

            assertEquals(exception, result.exceptionOrNull())
            coVerify {
                actionInteractor(
                    MultipleActionUpdateParams(
                        params =
                            listOf(
                                ActionUpdateSelectPaymentMethodParams(
                                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                                    cardNetwork = null,
                                ),
                            ),
                    ),
                )
                configurationInteractor.invoke(StripeAchConfigParams(PaymentMethodType.STRIPE_ACH.name))
            }
            coVerify(exactly = 0) {
                tokenizationInteractor(any())
            }
            verify {
                primerSettings.sdkIntegrationType
            }
        }
}
