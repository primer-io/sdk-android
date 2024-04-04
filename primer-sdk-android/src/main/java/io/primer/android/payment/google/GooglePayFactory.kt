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
            merchantName = settings.paymentMethodOptions.googlePayOptions.merchantName,
            totalPrice = PaymentUtils.minorToAmount(settings.currentAmount, currency).toString(),
            countryCode = settings.order.countryCode.toString(),
            currencyCode = currency.currencyCode,
            allowedCardNetworks = localConfigurationDataSource.getConfiguration().clientSession
                .paymentMethod?.orderedAllowedCardNetworks.orEmpty().intersect(
                    GooglePay.allowedCardNetworks
                ).toList(),
            buttonStyle = settings.paymentMethodOptions.googlePayOptions.buttonStyle,
            billingAddressRequired = settings.paymentMethodOptions.googlePayOptions.captureBillingAddress,
            existingPaymentMethodRequired = settings.paymentMethodOptions.googlePayOptions.existingPaymentMethodRequired
        )

        return Success(googlePay)
    }
}
