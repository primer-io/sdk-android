package io.primer.android.payment

import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.payment.apaya.ApayaDescriptor
import io.primer.android.payment.card.CreditCard
import io.primer.android.payment.klarna.KlarnaDescriptor
import io.primer.android.payment.paypal.PayPalDescriptor

internal class PaymentMethodDescriptorMapping(
    private val descriptors: List<PaymentMethodDescriptor>
) {

    fun getDescriptorFor(paymentMethod: String): PaymentMethodDescriptor? =
        when (paymentMethod) {
            PaymentMethodType.PAYMENT_CARD.name -> {
                descriptors.find { it is CreditCard }
            }

            PaymentMethodType.KLARNA.name -> {
                descriptors.find { it is KlarnaDescriptor }
            }

            PaymentMethodType.PAYPAL.name -> {
                descriptors.find { it is PayPalDescriptor }
            }

            PaymentMethodType.APAYA.name -> {
                descriptors.find { it is ApayaDescriptor }
            }

            else -> descriptors.find { it.config.type == paymentMethod }
        }
}
