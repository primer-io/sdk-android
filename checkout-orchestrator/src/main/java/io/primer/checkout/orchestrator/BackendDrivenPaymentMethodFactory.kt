package io.primer.checkout.orchestrator

import io.primer.android.core.utils.Either
import io.primer.android.core.utils.Failure
import io.primer.android.core.utils.Success
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.paymentmethods.PaymentMethod
import io.primer.android.paymentmethods.PaymentMethodFactory

class BackendDrivenPaymentMethodFactory(
    private val type: String,
    private val settings: PrimerSettings,
) : PaymentMethodFactory {
    override fun build(): Either<PaymentMethod, Exception> {
        return if (settings.paymentHandling == PrimerPaymentHandling.MANUAL) {
            Failure(Exception("$type is not supported in ${settings.paymentHandling} mode."))
        } else {
            Success(BackendDrivenPaymentMethod(type = type))
        }
    }
}
