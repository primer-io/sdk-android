package io.primer.android.payment.stripe.ach

import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.CompleteStripeAchPaymentSessionDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.StripeAchMandateTimestampLoggingDelegate
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.payments.create.repository.PaymentResultRepository
import io.primer.android.events.EventDispatcher
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertIs

@ExtendWith(MockKExtension::class)
class StripeAchPaymentMethodDescriptorFactoryTest {
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
    private lateinit var factory: StripeAchPaymentMethodDescriptorFactory

    @Test
    fun `create() should return StripeAchDescriptor`() {
        val descriptor = factory.create(
            localConfig = mockk(relaxed = true),
            paymentMethodRemoteConfig = mockk(relaxed = true),
            paymentMethod = mockk(),
            paymentMethodCheckers = mockk()
        )
        assertIs<StripeAchDescriptor>(descriptor)
    }
}
