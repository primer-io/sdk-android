package io.primer.android.stripe.ach

import io.primer.android.core.utils.Either
import io.primer.android.core.utils.Failure
import io.primer.android.core.utils.Success
import io.primer.android.paymentmethods.PaymentMethod
import io.primer.android.paymentmethods.PaymentMethodFactory
import io.primer.android.stripe.ach.implementation.helpers.StripeSdkClassValidator

class StripeAchFactory(private val type: String) : PaymentMethodFactory {
    override fun build(): Either<PaymentMethod, Exception> {
        val stripeAch = StripeAch(type = type)

        if (!StripeSdkClassValidator.isStripeSdkIncluded()) {
            return Failure(
                IllegalStateException(
                    StripeSdkClassValidator.STRIPE_CLASS_NOT_LOADED_ERROR,
                ),
            )
        }

        return Success(stripeAch)
    }
}
