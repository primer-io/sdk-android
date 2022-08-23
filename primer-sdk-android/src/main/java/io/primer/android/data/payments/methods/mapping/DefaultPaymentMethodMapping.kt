package io.primer.android.data.payments.methods.mapping

import io.primer.android.PaymentMethod
import io.primer.android.data.configuration.models.PaymentMethodImplementationType
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.payment.apaya.ApayaFactory
import io.primer.android.payment.async.AsyncMethodFactory
import io.primer.android.payment.card.CardFactory
import io.primer.android.payment.gocardless.GoCardlessFactory
import io.primer.android.payment.google.GooglePayFactory
import io.primer.android.payment.klarna.KlarnaFactory
import io.primer.android.payment.paypal.PayPalFactory
import io.primer.android.utils.Either
import io.primer.android.utils.Failure

internal interface PaymentMethodMapping {

    fun getPaymentMethodFor(
        implementationType: PaymentMethodImplementationType,
        type: String
    ): Either<PaymentMethod, Exception>
}

internal class DefaultPaymentMethodMapping(val settings: PrimerSettings) : PaymentMethodMapping {

    override fun getPaymentMethodFor(
        implementationType: PaymentMethodImplementationType,
        type: String
    ): Either<PaymentMethod, Exception> =
        when (implementationType) {
            PaymentMethodImplementationType.NATIVE_SDK -> {
                when (PaymentMethodType.safeValueOf(type)) {
                    PaymentMethodType.PAYMENT_CARD -> CardFactory().build()
                    PaymentMethodType.PRIMER_TEST_KLARNA,
                    PaymentMethodType.KLARNA -> KlarnaFactory(type, settings).build()
                    PaymentMethodType.GOOGLE_PAY -> GooglePayFactory(settings).build()
                    PaymentMethodType.PRIMER_TEST_PAYPAL,
                    PaymentMethodType.PAYPAL -> PayPalFactory(settings, type).build()
                    PaymentMethodType.GOCARDLESS -> GoCardlessFactory(settings).build()
                    PaymentMethodType.APAYA -> ApayaFactory(settings).build()
                    PaymentMethodType.PRIMER_TEST_SOFORT,
                    PaymentMethodType.ADYEN_IDEAL,
                    PaymentMethodType.ADYEN_DOTPAY,
                    PaymentMethodType.ADYEN_BLIK,
                    PaymentMethodType.XFERS_PAYNOW,
                    PaymentMethodType.ADYEN_MBWAY,
                    PaymentMethodType.RAPYD_FAST,
                    PaymentMethodType.RAPYD_PROMPTPAY,
                    PaymentMethodType.XENDIT_OVO -> AsyncMethodFactory(
                        type,
                        settings
                    ).build()
                    PaymentMethodType.ADYEN_BANK_TRANSFER,
                    PaymentMethodType.UNKNOWN -> Failure(
                        Exception("Unknown payment method, can't register.")
                    )
                    else -> Failure(Exception("Unknown payment method, can't register."))
                }
            }
            PaymentMethodImplementationType.WEB_REDIRECT ->
                AsyncMethodFactory(
                    type,
                    settings
                ).build()
            PaymentMethodImplementationType.UNKNOWN -> Failure(
                Exception(
                    "Unknown payment method implementation $implementationType," +
                        " can't register."
                )
            )
        }
}
