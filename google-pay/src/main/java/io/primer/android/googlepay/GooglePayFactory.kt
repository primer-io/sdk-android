package io.primer.android.googlepay

import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.core.utils.Either
import io.primer.android.core.utils.Failure
import io.primer.android.core.utils.Success
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.paymentmethods.PaymentMethod
import io.primer.android.paymentmethods.PaymentMethodFactory
import io.primer.android.payments.core.utils.PaymentUtils
import java.util.Currency

class GooglePayFactory(
    val settings: PrimerSettings,
    val configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
) : PaymentMethodFactory {
    override fun build(): Either<PaymentMethod, Exception> {
        val order = requireNotNull(configurationDataSource.get().clientSession.order)

        if (order.countryCode == null) {
            return Failure(Exception("Country code is null"))
        }

        val currency: Currency

        try {
            currency = Currency.getInstance(order.currencyCode)
        } catch (e: IllegalArgumentException) {
            return Failure(Exception(e.message))
        }

        val googlePay =
            GooglePay(
                merchantName = settings.paymentMethodOptions.googlePayOptions.merchantName,
                totalPrice = PaymentUtils.minorToAmount(order.currentAmount, currency).toString(),
                countryCode = order.countryCode.toString(),
                currencyCode = currency.currencyCode,
                allowedCardNetworks =
                    configurationDataSource.get().clientSession
                        .paymentMethod?.orderedAllowedCardNetworks.orEmpty().intersect(
                            GooglePay.allowedCardNetworks,
                        ).toList(),
                buttonStyle = settings.paymentMethodOptions.googlePayOptions.buttonStyle,
                billingAddressRequired = settings.paymentMethodOptions.googlePayOptions.captureBillingAddress,
                existingPaymentMethodRequired =
                    settings.paymentMethodOptions.googlePayOptions.existingPaymentMethodRequired,
            )

        return Success(googlePay)
    }
}
