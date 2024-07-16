package io.primer.android.payment.stripe.ach

import io.primer.android.PaymentMethod
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.CompleteStripeAchPaymentSessionDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.StripeAchMandateTimestampLoggingDelegate
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.payments.create.repository.PaymentResultRepository
import io.primer.android.events.EventDispatcher
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodDescriptorFactory
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry

internal class StripeAchPaymentMethodDescriptorFactory(
    private val eventDispatcher: EventDispatcher,
    private val paymentResultRepository: PaymentResultRepository,
    private val checkoutErrorEventResolver: BaseErrorEventResolver,
    private val completeStripeAchPaymentSessionDelegate: CompleteStripeAchPaymentSessionDelegate,
    private val stripeAchMandateTimestampLoggingDelegate: StripeAchMandateTimestampLoggingDelegate
) : PaymentMethodDescriptorFactory {

    override fun create(
        localConfig: PrimerConfig,
        paymentMethodRemoteConfig: PaymentMethodConfigDataResponse,
        paymentMethod: PaymentMethod,
        paymentMethodCheckers: PaymentMethodCheckerRegistry
    ): PaymentMethodDescriptor = StripeAchDescriptor(
        localConfig = localConfig,
        config = paymentMethodRemoteConfig,
        eventDispatcher = eventDispatcher,
        paymentResultRepository = paymentResultRepository,
        checkoutErrorEventResolver = checkoutErrorEventResolver,
        completeStripeAchPaymentSessionDelegate = completeStripeAchPaymentSessionDelegate,
        stripeAchMandateTimestampLoggingDelegate = stripeAchMandateTimestampLoggingDelegate
    )
}
