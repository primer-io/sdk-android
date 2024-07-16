package io.primer.android.payment.stripe.ach

import android.content.Context
import io.primer.android.PaymentMethod
import io.primer.android.PaymentMethodModule
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.CompleteStripeAchPaymentSessionDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.StripeAchMandateTimestampLoggingDelegate
import io.primer.android.data.configuration.models.ConfigurationData
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.payments.create.repository.PaymentResultRepository
import io.primer.android.events.EventDispatcher
import io.primer.android.payment.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry

internal data class StripeAch(
    override val type: String = PaymentMethodType.STRIPE_ACH.name,
    private val eventDispatcher: EventDispatcher,
    private val paymentResultRepository: PaymentResultRepository,
    private val checkoutErrorEventResolver: BaseErrorEventResolver,
    private val completeStripeAchPaymentSessionDelegate: CompleteStripeAchPaymentSessionDelegate,
    private val stripeAchMandateTimestampLoggingDelegate: StripeAchMandateTimestampLoggingDelegate
) : PaymentMethod {

    override val canBeVaulted: Boolean = true

    override val module: PaymentMethodModule = object : PaymentMethodModule {
        override fun initialize(applicationContext: Context, configuration: ConfigurationData) {
            // no-op
        }

        override fun registerPaymentMethodCheckers(
            paymentMethodCheckerRegistry: PaymentMethodCheckerRegistry
        ) {
            // no-op
        }

        override fun registerPaymentMethodDescriptorFactory(
            paymentMethodDescriptorFactoryRegistry: PaymentMethodDescriptorFactoryRegistry
        ) {
            paymentMethodDescriptorFactoryRegistry.register(
                type = type,
                factory = StripeAchPaymentMethodDescriptorFactory(
                    eventDispatcher = eventDispatcher,
                    paymentResultRepository = paymentResultRepository,
                    checkoutErrorEventResolver = checkoutErrorEventResolver,
                    completeStripeAchPaymentSessionDelegate =
                    completeStripeAchPaymentSessionDelegate,
                    stripeAchMandateTimestampLoggingDelegate =
                    stripeAchMandateTimestampLoggingDelegate
                )
            )
        }
    }
}
