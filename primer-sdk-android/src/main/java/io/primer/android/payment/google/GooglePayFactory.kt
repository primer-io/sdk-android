package io.primer.android.payment.google

import io.primer.android.PaymentMethod
import io.primer.android.data.payments.methods.mapping.PaymentMethodFactory
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.utils.Either
import io.primer.android.utils.Failure
import io.primer.android.utils.PaymentUtils
import io.primer.android.utils.Success
import java.util.Currency

internal class GooglePayFactory(val settings: PrimerSettings) : PaymentMethodFactory {

    override fun build(): Either<PaymentMethod, Exception> {
        val amount: Int
        try {
            amount = settings.currentAmount
        } catch (e: IllegalArgumentException) {
            return Failure(Exception(e.message))
        }

        if (amount == 0) {
            return Failure(Exception("Amount is zero"))
        }

        if (settings.order.countryCode == null) {
            return Failure(Exception("Country code is null"))
        }

        val currency: Currency

        try {
            currency = Currency.getInstance(settings.currency)
        } catch (e: IllegalArgumentException) {
            return Failure(Exception(e.message))
        }

        val googlePay = GooglePay(
            settings.paymentMethodOptions.googlePayOptions.merchantName,
            PaymentUtils.minorToAmount(settings.currentAmount, currency).toString(),
            settings.order.countryCode.toString(),
            currency.currencyCode,
            settings.paymentMethodOptions.googlePayOptions.allowedCardNetworks,
            settings.paymentMethodOptions.googlePayOptions.buttonStyle,
            settings.paymentMethodOptions.googlePayOptions.captureBillingAddress
        )

        return Success(googlePay)
    }
}
