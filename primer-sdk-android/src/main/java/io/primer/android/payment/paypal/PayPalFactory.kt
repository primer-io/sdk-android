package io.primer.android.payment.paypal

import io.primer.android.PaymentMethod
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.methods.mapping.PaymentMethodFactory
import io.primer.android.data.settings.PrimerSettings

import io.primer.android.utils.Either
import io.primer.android.utils.Failure
import io.primer.android.utils.Success
import java.util.Currency

internal class PayPalFactory(val settings: PrimerSettings, private val type: String) :
    PaymentMethodFactory {

    override fun build(): Either<PaymentMethod, Exception> {
        if (type == PaymentMethodType.PRIMER_TEST_PAYPAL.name) {
            return Success(PayPal(type))
        }
        val amount: Int
        try {
            amount = settings.currentAmount
        } catch (e: IllegalArgumentException) {
            return Failure(Exception(e.message))
        }

        if (amount == 0) {
            return Failure(Exception("Amount is zero"))
        }

        try {
            Currency.getInstance(settings.currency)
        } catch (e: IllegalArgumentException) {
            return Failure(Exception(e.message))
        }

        return Success(PayPal(type))
    }
}
