package io.primer.android.components.manager.ach

import androidx.lifecycle.ViewModelStoreOwner
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkObject
import io.mockk.verify
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.domain.core.validation.ValidationResult
import io.primer.android.components.domain.core.validation.ValidationRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.stripe.validation.resolvers.StripeInitValidationRulesResolver
import io.primer.android.components.presentation.paymentMethods.base.DefaultHeadlessManagerDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.StripeAchUserDetailsComponent
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.settings.PrimerStripeOptions
import io.primer.android.di.DISdkContext
import io.primer.android.di.DependencyContainer
import io.primer.android.di.RpcContainer
import io.primer.android.di.SdkContainer
import io.primer.android.domain.exception.UnsupportedPaymentMethodException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
class PrimerHeadlessUniversalCheckoutAchManagerTest {
    @MockK
    private lateinit var primerSettings: PrimerSettings

    @MockK
    private lateinit var headlessManagerDelegate: DefaultHeadlessManagerDelegate

    @MockK
    private lateinit var stripeInitValidationRulesResolver: StripeInitValidationRulesResolver

    @MockK
    private lateinit var viewModelStoreOwner: ViewModelStoreOwner

    @InjectMockKs
    private lateinit var manager: PrimerHeadlessUniversalCheckoutAchManager

    @BeforeEach
    fun setUp() {
        DISdkContext.sdkContainer = SdkContainer().apply {
            registerContainer(MockContainer())
        }
        manager = spyk(manager)
    }

    internal inner class MockContainer : DependencyContainer() {
        override fun registerInitialDependencies() {
            registerSingleton { primerSettings }
            registerSingleton { headlessManagerDelegate }
            registerSingleton { stripeInitValidationRulesResolver }
        }
    }

    @Test
    fun `provide() should throw exception when payment type is STRIPE_ACH and a rule returns failure`() {
        val primerStripeOptions = mockk<PrimerStripeOptions>()
        every {
            primerSettings.paymentMethodOptions.stripeOptions
        } returns primerStripeOptions

        val rule = mockk<ValidationRule<PrimerStripeOptions>> {
            every { validate(any()) } returns ValidationResult.Failure(Exception())
        }
        every { stripeInitValidationRulesResolver.resolve().rules } returns listOf(rule)

        assertThrows<java.lang.Exception> {
            manager.provide<StripeAchUserDetailsComponent>(PaymentMethodType.STRIPE_ACH.name)
        }
        verify {
            stripeInitValidationRulesResolver.resolve().rules
            primerSettings.paymentMethodOptions.stripeOptions
            rule.validate(primerStripeOptions)
        }
    }

    @Test
    fun `provide() should provide component instance when payment type is STRIPE_ACH`() {
        mockkObject(StripeAchUserDetailsComponent.Companion)
        val mockComponent = mockk<StripeAchUserDetailsComponent>()
        every {
            StripeAchUserDetailsComponent.Companion.provideInstance(any())
        } returns mockComponent
        val primerStripeOptions = mockk<PrimerStripeOptions>()
        every {
            primerSettings.paymentMethodOptions.stripeOptions
        } returns primerStripeOptions
        every { headlessManagerDelegate.init(any(), any()) } just Runs

        val rule = mockk<ValidationRule<PrimerStripeOptions>> {
            every { validate(any()) } returns ValidationResult.Success
        }
        every { stripeInitValidationRulesResolver.resolve().rules } returns listOf(rule)

        val component = manager.provide<StripeAchUserDetailsComponent>(PaymentMethodType.STRIPE_ACH.name)

        assertEquals(mockComponent, component)
        verify {
            DISdkContext.sdkContainer?.unregisterContainer<RpcContainer>()
            stripeInitValidationRulesResolver.resolve().rules
            primerSettings.paymentMethodOptions.stripeOptions
            rule.validate(primerStripeOptions)
            headlessManagerDelegate.init(
                PaymentMethodType.STRIPE_ACH.name,
                PrimerPaymentMethodManagerCategory.NATIVE_UI
            )
        }
        unmockkObject(StripeAchUserDetailsComponent.Companion)
    }

    @Test
    fun `provide() should throw UnsupportedPaymentMethodException when ClassCastException is caught`() {
        mockkObject(StripeAchUserDetailsComponent.Companion)
        every {
            StripeAchUserDetailsComponent.Companion.provideInstance(any())
        } throws ClassCastException()
        val primerStripeOptions = mockk<PrimerStripeOptions>()
        every {
            primerSettings.paymentMethodOptions.stripeOptions
        } returns primerStripeOptions
        every { headlessManagerDelegate.init(any(), any()) } just Runs
        val rule = mockk<ValidationRule<PrimerStripeOptions>> {
            every { validate(any()) } returns ValidationResult.Success
        }
        every { stripeInitValidationRulesResolver.resolve().rules } returns listOf(rule)

        assertThrows<UnsupportedPaymentMethodException> {
            manager.provide<StripeAchUserDetailsComponent>(PaymentMethodType.STRIPE_ACH.name)
        }

        verify {
            stripeInitValidationRulesResolver.resolve().rules
            primerSettings.paymentMethodOptions.stripeOptions
            rule.validate(primerStripeOptions)
            headlessManagerDelegate.init(
                PaymentMethodType.STRIPE_ACH.name,
                PrimerPaymentMethodManagerCategory.NATIVE_UI
            )
        }
        unmockkObject(StripeAchUserDetailsComponent.Companion)
    }

    @Test
    fun `provide() should throw exception when payment type is not STRIPE_ACH`() {
        assertThrows<UnsupportedPaymentMethodException> {
            manager.provide<StripeAchUserDetailsComponent>(
                PaymentMethodType.ADYEN_IDEAL.name
            )
        }

        verify(exactly = 0) {
            stripeInitValidationRulesResolver.resolve().rules
            primerSettings.paymentMethodOptions.stripeOptions.publishableKey
            headlessManagerDelegate.init(any(), any())
        }
    }
}
