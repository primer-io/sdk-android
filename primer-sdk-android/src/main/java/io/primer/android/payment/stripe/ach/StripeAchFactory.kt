package io.primer.android.payment.stripe.ach

import io.primer.android.PaymentMethod
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.CompleteStripeAchPaymentSessionDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.StripeAchMandateTimestampLoggingDelegate
import io.primer.android.data.payments.methods.mapping.PaymentMethodFactory
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.payments.create.repository.PaymentResultRepository
import io.primer.android.events.EventDispatcher
import io.primer.android.payment.stripe.helpers.StripeSdkClassValidator
import io.primer.android.utils.Either
import io.primer.android.utils.Failure
import io.primer.android.utils.Success

internal class StripeAchFactory(
    private val type: String,
    private val eventDispatcher: EventDispatcher,
    private val paymentResultRepository: PaymentResultRepository,
    private val checkoutErrorEventResolver: BaseErrorEventResolver,
    private val completeStripeAchPaymentSessionDelegate: CompleteStripeAchPaymentSessionDelegate,
    private val stripeAchMandateTimestampLoggingDelegate: StripeAchMandateTimestampLoggingDelegate
) : PaymentMethodFactory {

    override fun build(): Either<PaymentMethod, Exception> {
        val stripeAch = StripeAch(
            type = type,
            eventDispatcher = eventDispatcher,
            paymentResultRepository = paymentResultRepository,
            checkoutErrorEventResolver = checkoutErrorEventResolver,
            completeStripeAchPaymentSessionDelegate = completeStripeAchPaymentSessionDelegate,
            stripeAchMandateTimestampLoggingDelegate = stripeAchMandateTimestampLoggingDelegate
        )

        if (!StripeSdkClassValidator.isStripeSdkIncluded()) {
            return Failure(
                IllegalStateException(
                    StripeSdkClassValidator.STRIPE_CLASS_NOT_LOADED_ERROR
                )
            )
        }

        return Success(stripeAch)
    }
}
