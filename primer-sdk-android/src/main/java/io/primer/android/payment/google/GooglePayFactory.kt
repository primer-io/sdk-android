package io.primer.android.payment.google

import io.primer.android.PaymentMethod
import io.primer.android.model.dto.PrimerSettings
import io.primer.android.payment.PaymentMethodFactory
import io.primer.android.utils.Either
import io.primer.android.utils.Failure
import io.primer.android.utils.PaymentUtils
import io.primer.android.utils.Success
import java.util.Currency

internal class GooglePayFactory(val settings: PrimerSettings) : PaymentMethodFactory() {

    override fun build(): Either<PaymentMethod, Exception> {
        if (settings.order.amount == null) {
            return Failure(Exception("Amount is null"))
        }

        if (settings.order.countryCode == null) {
            return Failure(Exception("Country code is null"))
        }

        if (settings.order.currency == null) {
            return Failure(Exception("Currency is null"))
        }

        try {
            Currency.getInstance(settings.order.currency)
        } catch (e: IllegalArgumentException) {
            return Failure(Exception(e.message))
        }

        val googlePay = GooglePay(
            settings.business.name,
            PaymentUtils.minorToAmount(
                settings.order.amount!!,
                Currency.getInstance(settings.order.currency)
            ).toString(),
            settings.order.countryCode.toString(),
            settings.order.currency.toString(),
            settings.options.googlePayAllowedCardNetworks,
            settings.options.googlePayButtonStyle,
        )

        return Success(googlePay)
    }
}
