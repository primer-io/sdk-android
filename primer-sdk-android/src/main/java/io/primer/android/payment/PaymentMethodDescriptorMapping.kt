package io.primer.android.payment

import io.primer.android.data.configuration.models.PrimerPaymentMethodType
import io.primer.android.payment.apaya.ApayaDescriptor
import io.primer.android.payment.card.CreditCard
import io.primer.android.payment.gocardless.GoCardlessDescriptor
import io.primer.android.payment.google.GooglePayDescriptor
import io.primer.android.payment.klarna.KlarnaDescriptor
import io.primer.android.payment.paypal.PayPalDescriptor
import org.koin.core.component.KoinApiExtension

internal class PaymentMethodDescriptorMapping(
    private val descriptors: List<PaymentMethodDescriptor>
) {

    @KoinApiExtension
    fun getDescriptorFor(paymentMethod: PrimerPaymentMethodType): PaymentMethodDescriptor? =
        when (paymentMethod) {
            PrimerPaymentMethodType.PAYMENT_CARD -> {
                descriptors.find { it is CreditCard }
            }

            PrimerPaymentMethodType.KLARNA -> {
                descriptors.find { it is KlarnaDescriptor }
            }

            PrimerPaymentMethodType.PAYPAL -> {
                descriptors.find { it is PayPalDescriptor }
            }

            PrimerPaymentMethodType.GOOGLE_PAY -> {
                descriptors.find { it is GooglePayDescriptor }
            }

            PrimerPaymentMethodType.GOCARDLESS -> {
                descriptors.find { it is GoCardlessDescriptor }
            }

            PrimerPaymentMethodType.APAYA -> {
                descriptors.find { it is ApayaDescriptor }
            }

            else -> descriptors.find { it.config.type == paymentMethod }
        }
}
