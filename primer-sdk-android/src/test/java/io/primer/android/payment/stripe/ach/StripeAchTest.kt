package io.primer.android.payment.stripe.ach

import io.mockk.confirmVerified
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.CompleteStripeAchPaymentSessionDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.StripeAchMandateTimestampLoggingDelegate
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.payments.create.repository.PaymentResultRepository
import io.primer.android.events.EventDispatcher
import io.primer.android.payment.PaymentMethodDescriptorFactoryRegistry
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class StripeAchTest {
    @MockK
    private lateinit var eventDispatcher: EventDispatcher

    @MockK
    private lateinit var paymentResultRepository: PaymentResultRepository

    @MockK
    private lateinit var checkoutErrorEventResolver: BaseErrorEventResolver

    @MockK
    private lateinit var completeStripeAchPaymentSessionDelegate: CompleteStripeAchPaymentSessionDelegate

    @MockK
    private lateinit var stripeAchMandateTimestampLoggingDelegate: StripeAchMandateTimestampLoggingDelegate

    @InjectMockKs
    private lateinit var stripeAch: StripeAch

    @Test
    fun `canBeVaulted should return true`() {
        assertTrue(stripeAch.canBeVaulted)
    }

    @Test
    fun `module returns PaymentMethodModule implementation with empty initialize()`() {
        val module = stripeAch.module

        module.initialize(mockk(), mockk())

        confirmVerified(
            eventDispatcher,
            paymentResultRepository,
            checkoutErrorEventResolver,
            completeStripeAchPaymentSessionDelegate,
            stripeAchMandateTimestampLoggingDelegate
        )
    }

    @Test
    fun `module returns PaymentMethodModule implementation with empty registerPaymentMethodCheckers()`() {
        val module = stripeAch.module

        module.registerPaymentMethodCheckers(mockk())

        confirmVerified(
            eventDispatcher,
            paymentResultRepository,
            checkoutErrorEventResolver,
            completeStripeAchPaymentSessionDelegate,
            stripeAchMandateTimestampLoggingDelegate
        )
    }

    @Test
    fun `module returns PaymentMethodModule implementation with registerPaymentMethodDescriptorFactory() implementation that registers StripeAchPaymentMethodDescriptorFactory`() {
        val module = stripeAch.module
        val registry = mockk<PaymentMethodDescriptorFactoryRegistry>(relaxed = true)

        module.registerPaymentMethodDescriptorFactory(registry)

        verify {
            registry.register(PaymentMethodType.STRIPE_ACH.name, any<StripeAchPaymentMethodDescriptorFactory>())
        }
        confirmVerified(
            eventDispatcher,
            paymentResultRepository,
            checkoutErrorEventResolver,
            completeStripeAchPaymentSessionDelegate,
            stripeAchMandateTimestampLoggingDelegate
        )
    }
}
