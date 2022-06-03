package io.primer.android.data.payments.methods.error

import io.primer.android.data.error.DefaultErrorMapper
import io.primer.android.domain.error.models.PaymentMethodError
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.exception.MissingPaymentMethodException
import io.primer.android.domain.exception.UnsupportedPaymentIntentException
import io.primer.android.domain.exception.UnsupportedPaymentMethodException

internal class PaymentMethodsErrorMapper : DefaultErrorMapper() {

    override fun getPrimerError(throwable: Throwable): PrimerError {
        return when (throwable) {
            is MissingPaymentMethodException ->
                PaymentMethodError.MisConfiguredPaymentMethodError(
                    throwable.paymentMethodType
                )
            is UnsupportedPaymentIntentException ->
                PaymentMethodError.UnsupportedIntentPaymentMethodError(
                    throwable.paymentMethodType,
                    throwable.primerIntent
                )
            is UnsupportedPaymentMethodException ->
                PaymentMethodError.UnsupportedPaymentMethodError(
                    throwable.paymentMethodType,
                )
            else -> return super.getPrimerError(throwable)
        }
    }
}
