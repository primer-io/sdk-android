package io.primer.android.components.ach

import android.content.Context
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
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.PaymentMethodManagerDelegate
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.core.domain.validation.ValidationRule
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.settings.PrimerStripeOptions
import io.primer.android.domain.exception.UnsupportedPaymentMethodException
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.stripe.ach.api.component.StripeAchUserDetailsComponent
import io.primer.android.stripe.ach.implementation.validation.resolvers.StripeInitValidationRulesResolver
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
    private lateinit var viewModelStoreOwner: ViewModelStoreOwner

    @MockK
    private lateinit var paymentMethodInitializer: PaymentMethodManagerDelegate

    @MockK
    private lateinit var stripeInitValidationRulesResolver: StripeInitValidationRulesResolver

    @InjectMockKs
    private lateinit var manager: PrimerHeadlessUniversalCheckoutAchManager

    @BeforeEach
    fun setUp() {
        DISdkContext.headlessSdkContainer =
            SdkContainer().apply {
                registerContainer(MockContainer())
            }
        manager = spyk(manager)
    }

    internal inner class MockContainer : DependencyContainer() {
        override fun registerInitialDependencies() {
            registerSingleton { primerSettings }
            registerSingleton { paymentMethodInitializer }
            registerSingleton { stripeInitValidationRulesResolver }
            registerSingleton { mockk<Context>() }
        }
    }

    @Test
    fun `provide() should throw exception when payment type is STRIPE_ACH and a rule returns failure`() {
        val primerStripeOptions = mockk<PrimerStripeOptions>()
        every {
            primerSettings.paymentMethodOptions.stripeOptions
        } returns primerStripeOptions

        val rule =
            mockk<ValidationRule<PrimerStripeOptions>> {
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
        every { paymentMethodInitializer.init(any(), any()) } just Runs
        every { paymentMethodInitializer.start(any(), any(), any(), any()) } just Runs

        val rule =
            mockk<ValidationRule<PrimerStripeOptions>> {
                every { validate(any()) } returns ValidationResult.Success
            }
        every { stripeInitValidationRulesResolver.resolve().rules } returns listOf(rule)

        val component = manager.provide<StripeAchUserDetailsComponent>(PaymentMethodType.STRIPE_ACH.name)

        assertEquals(mockComponent, component)
        verify {
            stripeInitValidationRulesResolver.resolve().rules
            primerSettings.paymentMethodOptions.stripeOptions
            rule.validate(primerStripeOptions)
            paymentMethodInitializer.init(
                PaymentMethodType.STRIPE_ACH.name,
                PrimerPaymentMethodManagerCategory.STRIPE_ACH,
            )
            paymentMethodInitializer.start(
                context = any(),
                paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                sessionIntent = PrimerSessionIntent.CHECKOUT,
                category = PrimerPaymentMethodManagerCategory.STRIPE_ACH,
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
        every { paymentMethodInitializer.init(any(), any()) } just Runs
        val rule =
            mockk<ValidationRule<PrimerStripeOptions>> {
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
            paymentMethodInitializer.init(
                PaymentMethodType.STRIPE_ACH.name,
                PrimerPaymentMethodManagerCategory.STRIPE_ACH,
            )
        }
        unmockkObject(StripeAchUserDetailsComponent.Companion)
    }

    @Test
    fun `provide() should throw exception when payment type is not STRIPE_ACH`() {
        assertThrows<UnsupportedPaymentMethodException> {
            manager.provide<StripeAchUserDetailsComponent>(
                PaymentMethodType.ADYEN_IDEAL.name,
            )
        }

        verify(exactly = 0) {
            stripeInitValidationRulesResolver.resolve().rules
            primerSettings.paymentMethodOptions.stripeOptions.publishableKey
            paymentMethodInitializer.init(any(), any())
        }
    }
}
