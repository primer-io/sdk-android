package io.primer.android.payment.google

import io.primer.android.PaymentMethod
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.payments.methods.mapping.PaymentMethodFactory
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.utils.Either
import io.primer.android.utils.Failure
import io.primer.android.utils.PaymentUtils
import io.primer.android.utils.Success
import java.util.Currency

internal class GooglePayFactory(
    private val settings: PrimerSettings,
    private val localConfigurationDataSource: LocalConfigurationDataSource
) : PaymentMethodFactory {

    override fun build(): Either<PaymentMethod, Exception> {
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
            localConfigurationDataSource.getConfiguration().clientSession
                .paymentMethod?.orderedAllowedCardNetworks.orEmpty().intersect(
                    GooglePay.allowedCardNetworks
                ).toList(),
            settings.paymentMethodOptions.googlePayOptions.buttonStyle,
            settings.paymentMethodOptions.googlePayOptions.captureBillingAddress
        )

        return Success(googlePay)
    }
}
