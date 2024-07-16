package io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.analytics.data.models.SdkIntegrationType
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.action.ActionInteractor
import io.primer.android.domain.action.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.domain.payments.methods.PaymentMethodModulesInteractor
import io.primer.android.domain.tokenization.TokenizationInteractor
import io.primer.android.domain.tokenization.models.TokenizationParamsV2
import io.primer.android.domain.tokenization.models.paymentInstruments.stripe.ach.StripeAchPaymentInstrumentParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
class StripeAchTokenizationDelegateTest {
    @MockK
    private lateinit var primerSettings: PrimerSettings

    @MockK
    private lateinit var actionInteractor: ActionInteractor

    @MockK
    private lateinit var paymentMethodModulesInteractor: PaymentMethodModulesInteractor

    @MockK
    private lateinit var tokenizationInteractor: TokenizationInteractor

    @InjectMockKs
    private lateinit var delegate: StripeAchTokenizationDelegate

    @BeforeEach
    fun setUp() {
        confirmVerified(paymentMethodModulesInteractor, tokenizationInteractor, actionInteractor)
    }

    @Test
    fun `invoke() should perform tokenization via interactor, select payment method and return success when interactor calls succeed and integration type is Headless`() = runTest {
        every { actionInteractor(any()) } returns emptyFlow()
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
        every { paymentMethodModulesInteractor.getPaymentMethodDescriptors() } returns listOf(
            mockk {
                every { config.id } returns "config_id"
                every { config.type } returns PaymentMethodType.STRIPE_ACH.name
                every { localConfig.settings.locale.toLanguageTag() } returns "language_tag"
            }
        )
        every { tokenizationInteractor.executeV2(any()) } returns flowOf("token")

        val result = delegate.invoke()

        assertEquals(Unit, result.getOrNull())
        verify {
            primerSettings.sdkIntegrationType
            paymentMethodModulesInteractor.getPaymentMethodDescriptors()
        }
        coVerify {
            actionInteractor(
                ActionUpdateSelectPaymentMethodParams(
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                    cardNetwork = null
                )
            )
            tokenizationInteractor.executeV2(
                TokenizationParamsV2(
                    paymentInstrumentParams = StripeAchPaymentInstrumentParams(
                        "config_id",
                        "language_tag"
                    ),
                    paymentMethodIntent = null
                )
            )
        }
    }

    @Test
    fun `invoke() should perform tokenization via interactor, not select payment method and return success when interactor calls succeed and integration type is Drop-in`() = runTest {
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.DROP_IN
        every { paymentMethodModulesInteractor.getPaymentMethodDescriptors() } returns listOf(
            mockk {
                every { config.id } returns "config_id"
                every { config.type } returns PaymentMethodType.STRIPE_ACH.name
                every { localConfig.settings.locale.toLanguageTag() } returns "language_tag"
            }
        )
        every { tokenizationInteractor.executeV2(any()) } returns flowOf("token")

        val result = delegate.invoke()

        assertEquals(Unit, result.getOrNull())
        verify {
            primerSettings.sdkIntegrationType
            paymentMethodModulesInteractor.getPaymentMethodDescriptors()
        }
        coVerify {
            tokenizationInteractor.executeV2(
                TokenizationParamsV2(
                    paymentInstrumentParams = StripeAchPaymentInstrumentParams(
                        "config_id",
                        "language_tag"
                    ),
                    paymentMethodIntent = null
                )
            )
        }
    }

    @Test
    fun `invoke() should return failure when there are no descriptors for Stripe ACH`() = runTest {
        every { actionInteractor(any()) } returns emptyFlow()
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
        every { paymentMethodModulesInteractor.getPaymentMethodDescriptors() } returns listOf(
            mockk {
                every { config.type } returns PaymentMethodType.KLARNA.name
            }
        )

        val result = delegate.invoke().exceptionOrNull()

        assertInstanceOf(NoSuchElementException::class.java, result)
        verify {
            primerSettings.sdkIntegrationType
            paymentMethodModulesInteractor.getPaymentMethodDescriptors()
        }
        coVerify {
            actionInteractor(
                ActionUpdateSelectPaymentMethodParams(
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                    cardNetwork = null
                )
            )
        }
    }

    @Test
    fun `invoke() should return failure when the action interactor call fails`() = runTest {
        val exception = Exception()
        every { actionInteractor(any()) } returns flow { throw exception }
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS

        val result = delegate.invoke().exceptionOrNull()

        assertEquals(exception, result)
        verify {
            primerSettings.sdkIntegrationType
        }
        coVerify {
            actionInteractor(
                ActionUpdateSelectPaymentMethodParams(
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                    cardNetwork = null
                )
            )
        }
    }

    @Test
    fun `invoke() should return failure when the tokenization interactor call fails`() = runTest {
        every { actionInteractor(any()) } returns emptyFlow()
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
        every { paymentMethodModulesInteractor.getPaymentMethodDescriptors() } returns listOf(
            mockk {
                every { config.id } returns "config_id"
                every { config.type } returns PaymentMethodType.STRIPE_ACH.name
                every { localConfig.settings.locale.toLanguageTag() } returns "language_tag"
            }
        )
        val exception = Exception()
        every { tokenizationInteractor.executeV2(any()) } returns flow { throw exception }

        val result = delegate.invoke().exceptionOrNull()

        assertEquals(exception, result)
        verify {
            primerSettings.sdkIntegrationType
            paymentMethodModulesInteractor.getPaymentMethodDescriptors()
        }
        coVerify {
            actionInteractor(
                ActionUpdateSelectPaymentMethodParams(
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                    cardNetwork = null
                )
            )
            tokenizationInteractor.executeV2(
                TokenizationParamsV2(
                    paymentInstrumentParams = StripeAchPaymentInstrumentParams(
                        "config_id",
                        "language_tag"
                    ),
                    paymentMethodIntent = null
                )
            )
        }
    }
}
