package io.primer.android.data.payments.methods.mapping

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.primer.android.InstantExecutorExtension
import io.primer.android.PaymentMethod
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.CompleteStripeAchPaymentSessionDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.StripeAchMandateTimestampLoggingDelegate
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.models.PaymentMethodImplementationType
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.payments.create.repository.PaymentResultRepository
import io.primer.android.events.EventDispatcher
import io.primer.android.payment.stripe.ach.StripeAch
import io.primer.android.payment.stripe.helpers.StripeSdkClassValidator
import io.primer.android.utils.Failure
import io.primer.android.utils.Success
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
class DefaultPaymentMethodMappingTest {
    @MockK
    private lateinit var settings: PrimerSettings

    @MockK
    private lateinit var localConfigurationDataSource: LocalConfigurationDataSource

    @MockK
    private lateinit var eventDispatcher: EventDispatcher

    @MockK
    private lateinit var paymentResultRepository: PaymentResultRepository

    @MockK
    private lateinit var checkoutErrorEventResolver: BaseErrorEventResolver

    @MockK
    private lateinit var completeStripeAchPaymentSessionDelegate:
        CompleteStripeAchPaymentSessionDelegate

    @MockK
    private lateinit var stripeAchMandateTimestampLoggingDelegate:
        StripeAchMandateTimestampLoggingDelegate

    @InjectMockKs
    private lateinit var mapping: DefaultPaymentMethodMapping

    @Test
    fun `getPaymentMethodFor() return success when implementation type is NATIVE_SDK, type is STRIPE_ACH and Stripe SDK is included`() {
        mockkObject(StripeSdkClassValidator)
        every { StripeSdkClassValidator.isStripeSdkIncluded() } returns true

        val paymentMethod = mapping.getPaymentMethodFor(
            implementationType = PaymentMethodImplementationType.NATIVE_SDK,
            type = PaymentMethodType.STRIPE_ACH.name
        )

        assertEquals(
            Success<PaymentMethod, Exception>(
                StripeAch(
                    type = PaymentMethodType.STRIPE_ACH.name,
                    eventDispatcher = eventDispatcher,
                    paymentResultRepository = paymentResultRepository,
                    checkoutErrorEventResolver = checkoutErrorEventResolver,
                    completeStripeAchPaymentSessionDelegate =
                    completeStripeAchPaymentSessionDelegate,
                    stripeAchMandateTimestampLoggingDelegate =
                    stripeAchMandateTimestampLoggingDelegate
                )
            ),
            paymentMethod
        )
        unmockkObject(StripeSdkClassValidator)
    }

    @Test
    fun `getPaymentMethodFor() return failure when implementation type is NATIVE_SDK, type is STRIPE_ACH and Stripe SDK is not included`() {
        mockkObject(StripeSdkClassValidator)
        every { StripeSdkClassValidator.isStripeSdkIncluded() } returns false

        val paymentMethod = mapping.getPaymentMethodFor(
            implementationType = PaymentMethodImplementationType.NATIVE_SDK,
            type = PaymentMethodType.STRIPE_ACH.name
        )

        assertInstanceOf(Failure::class.java, paymentMethod)
        assertEquals(
            StripeSdkClassValidator.STRIPE_CLASS_NOT_LOADED_ERROR,
            (paymentMethod as Failure).value.message
        )
        unmockkObject(StripeSdkClassValidator)
    }
}
