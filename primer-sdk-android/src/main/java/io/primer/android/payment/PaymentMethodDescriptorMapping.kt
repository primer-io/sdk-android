package io.primer.android.payment

import io.primer.android.model.dto.PrimerPaymentMethod
import io.primer.android.model.dto.toPrimerPaymentMethod
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
    fun getDescriptorFor(paymentMethod: PrimerPaymentMethod): PaymentMethodDescriptor? =
        when (paymentMethod) {
            PrimerPaymentMethod.CARD -> {
                descriptors.find { it is CreditCard }
            }

            PrimerPaymentMethod.KLARNA -> {
                descriptors.find { it is KlarnaDescriptor }
            }

            PrimerPaymentMethod.PAYPAL -> {
                descriptors.find { it is PayPalDescriptor }
            }

            PrimerPaymentMethod.GOOGLE_PAY -> {
                descriptors.find { it is GooglePayDescriptor }
            }

            PrimerPaymentMethod.GOCARDLESS -> {
                descriptors.find { it is GoCardlessDescriptor }
            }

            PrimerPaymentMethod.APAYA -> {
                descriptors.find { it is ApayaDescriptor }
            }

            else -> descriptors.find { it.config.type.toPrimerPaymentMethod() == paymentMethod }
        }
}
