package io.primer.android.components

import io.primer.android.bancontact.AdyenBancontactFactory
import io.primer.android.banks.BankIssuerFactory
import io.primer.android.card.CardFactory
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.configuration.data.model.PaymentMethodImplementationType
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.core.utils.Either
import io.primer.android.core.utils.Failure
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.googlepay.GooglePayFactory
import io.primer.android.ipay88.IPay88PaymentMethodFactory
import io.primer.android.klarna.KlarnaFactory
import io.primer.android.nolpay.NolPayFactory
import io.primer.android.otp.OtpFactory
import io.primer.android.paymentmethods.PaymentMethod
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paypal.PayPalFactory
import io.primer.android.phoneNumber.PhoneNumberFactory
import io.primer.android.qrcode.QrCodeFactory
import io.primer.android.sandboxProcessor.klarna.SandboxProcessorKlarnaFactory
import io.primer.android.sandboxProcessor.paypal.SandboxProcessorPayPalFactory
import io.primer.android.stripe.ach.StripeAchFactory
import io.primer.android.vouchers.multibanco.MultibancoFactory
import io.primer.android.vouchers.retailOutlets.RetailOutletsFactory
import io.primer.android.webredirect.WebRedirectFactory

internal fun interface PaymentMethodMapping {
    fun getPaymentMethodFor(
        implementationType: PaymentMethodImplementationType,
        type: String,
    ): Either<PaymentMethod, Exception>
}

internal class DefaultPaymentMethodMapping(
    private val settings: PrimerSettings,
    private val configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
) : PaymentMethodMapping {
    @Suppress("CyclomaticComplexMethod", "LongMethod")
    override fun getPaymentMethodFor(
        implementationType: PaymentMethodImplementationType,
        type: String,
    ): Either<PaymentMethod, Exception> =
        when (implementationType) {
            PaymentMethodImplementationType.NATIVE_SDK -> {
                when (PaymentMethodType.safeValueOf(type)) {
                    PaymentMethodType.PAYMENT_CARD -> CardFactory().build()
                    PaymentMethodType.PRIMER_TEST_KLARNA -> SandboxProcessorKlarnaFactory(type).build()

                    PaymentMethodType.KLARNA -> KlarnaFactory(type).build()

                    PaymentMethodType.STRIPE_ACH -> StripeAchFactory(type).build()

                    PaymentMethodType.GOOGLE_PAY ->
                        GooglePayFactory(
                            settings,
                            configurationDataSource,
                        ).build()

                    PaymentMethodType.PRIMER_TEST_PAYPAL -> SandboxProcessorPayPalFactory(type).build()
                    PaymentMethodType.PAYPAL -> PayPalFactory(type).build()

                    PaymentMethodType.ADYEN_IDEAL,
                    PaymentMethodType.ADYEN_DOTPAY,
                    -> BankIssuerFactory(type).build()

                    PaymentMethodType.ADYEN_BLIK -> OtpFactory(type).build()
                    PaymentMethodType.ADYEN_BANCONTACT_CARD -> AdyenBancontactFactory().build()
                    PaymentMethodType.ADYEN_MBWAY,
                    PaymentMethodType.XENDIT_OVO,
                    ->
                        PhoneNumberFactory(
                            paymentMethodType = type,
                        ).build()

                    PaymentMethodType.XFERS_PAYNOW,
                    // PaymentMethodType.RAPYD_FAST, // TODO TWS: perhaps use a new factory
                    PaymentMethodType.RAPYD_PROMPTPAY,
                    PaymentMethodType.OMISE_PROMPTPAY,
                    ->
                        QrCodeFactory(
                            paymentMethodType = type,
                        ).build()

                    PaymentMethodType.ADYEN_MULTIBANCO ->
                        MultibancoFactory(
                            paymentMethodType = type,
                        ).build()

                    PaymentMethodType.XENDIT_RETAIL_OUTLETS ->
                        RetailOutletsFactory(
                            paymentMethodType = type,
                        ).build()

                    PaymentMethodType.NOL_PAY -> NolPayFactory().build()
                    PaymentMethodType.UNKNOWN ->
                        Failure(
                            Exception("Unknown payment method, can't register."),
                        )

                    else -> Failure(Exception("Unknown payment method, can't register."))
                }
            }

            PaymentMethodImplementationType.WEB_REDIRECT ->
                WebRedirectFactory(type).build()

            PaymentMethodImplementationType.IPAY88_SDK ->
                IPay88PaymentMethodFactory(
                    type = type,
                    configurationDataSource = configurationDataSource,
                ).build()

            PaymentMethodImplementationType.UNKNOWN ->
                Failure(
                    Exception(
                        "Unknown payment method implementation $implementationType," +
                            " can't register.",
                    ),
                )
        }
}
