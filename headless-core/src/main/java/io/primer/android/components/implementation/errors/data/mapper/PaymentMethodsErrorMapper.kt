package io.primer.android.components.implementation.errors.data.mapper

import io.primer.android.components.implementation.errors.domain.model.PaymentMethodConfigurationError
import io.primer.android.domain.exception.MissingPaymentMethodException
import io.primer.android.domain.exception.UnsupportedPaymentIntentException
import io.primer.android.domain.exception.UnsupportedPaymentMethodException
import io.primer.android.errors.domain.ErrorMapper
import io.primer.android.domain.error.models.PrimerError

internal class PaymentMethodsErrorMapper : ErrorMapper {

    override fun getPrimerError(throwable: Throwable): PrimerError {
        return when (throwable) {
            is MissingPaymentMethodException ->
                PaymentMethodConfigurationError.MisConfiguredPaymentMethodError(
                    throwable.paymentMethodType
                )

            is UnsupportedPaymentIntentException ->
                PaymentMethodConfigurationError.UnsupportedIntentPaymentMethodError(
                    throwable.paymentMethodType,
                    throwable.primerIntent
                )

            is UnsupportedPaymentMethodException ->
                PaymentMethodConfigurationError.UnsupportedPaymentMethodError(
                    throwable.paymentMethodType
                )

            else -> error("Unsupported mapping for $throwable in ${this.javaClass.canonicalName}")
        }
    }
}
