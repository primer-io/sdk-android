package io.primer.android.components.ui.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import io.primer.android.R
import io.primer.android.model.dto.PaymentMethodType

internal class PrimerPaymentMethodViewFactory(val context: Context) {

    fun getViewForPaymentMethod(paymentMethodType: PaymentMethodType): View? {
        val layoutId = when (paymentMethodType) {
            PaymentMethodType.PAYPAL -> R.layout.payment_method_button_paypal
            PaymentMethodType.GOOGLE_PAY -> R.layout.googlepay_black_button
            PaymentMethodType.APAYA -> R.layout.payment_method_button_pay_mobile
            PaymentMethodType.KLARNA -> R.layout.payment_method_button_klarna
            PaymentMethodType.ATOME -> R.layout.payment_method_button_atome
            PaymentMethodType.PAYMENT_CARD -> R.layout.payment_method_button_card
            PaymentMethodType.GOCARDLESS -> R.layout.payment_method_button_direct_debit
            PaymentMethodType.PAY_NL_IDEAL -> R.layout.payment_method_button_ideal
            PaymentMethodType.PAY_NL_PAYCONIQ -> R.layout.payment_method_button_payconiq
            PaymentMethodType.PAY_NL_GIROPAY -> R.layout.payment_method_button_giropay
            PaymentMethodType.PAY_NL_P24 -> R.layout.payment_method_button_p24
            PaymentMethodType.PAY_NL_EPS -> R.layout.payment_method_button_eps
            PaymentMethodType.HOOLAH -> R.layout.payment_method_button_hoolah
            PaymentMethodType.ADYEN_GIROPAY -> R.layout.payment_method_button_giropay
            PaymentMethodType.ADYEN_TWINT -> R.layout.payment_method_button_twint
            PaymentMethodType.ADYEN_SOFORT -> R.layout.payment_method_button_sofort
            PaymentMethodType.ADYEN_TRUSTLY -> R.layout.payment_method_button_trustly
            PaymentMethodType.ADYEN_ALIPAY -> R.layout.payment_method_button_alipay
            PaymentMethodType.ADYEN_VIPPS -> R.layout.payment_method_button_vipps
            PaymentMethodType.ADYEN_MOBILEPAY -> R.layout.payment_method_button_mobilepay
            PaymentMethodType.ADYEN_IDEAL -> R.layout.payment_method_button_ideal
            PaymentMethodType.ADYEN_DOTPAY -> R.layout.payment_method_button_dotpay
            PaymentMethodType.ADYEN_BLIK -> R.layout.payment_method_button_blik
            PaymentMethodType.ADYEN_INTERAC -> R.layout.payment_method_button_interac
            PaymentMethodType.ADYEN_PAYTRAIL -> R.layout.payment_method_button_paytrail
            PaymentMethodType.ADYEN_PAYSHOP -> R.layout.payment_method_button_payshop
            PaymentMethodType.MOLLIE_BANCONTACT -> R.layout.payment_method_button_bancontact
            PaymentMethodType.MOLLIE_IDEAL -> R.layout.payment_method_button_ideal
            PaymentMethodType.MOLLIE_P24 -> R.layout.payment_method_button_p24
            PaymentMethodType.MOLLIE_GIROPAY -> R.layout.payment_method_button_giropay
            PaymentMethodType.MOLLIE_EPS -> R.layout.payment_method_button_eps
            PaymentMethodType.BUCKAROO_GIROPAY -> R.layout.payment_method_button_giropay
            PaymentMethodType.BUCKAROO_SOFORT -> R.layout.payment_method_button_sofort
            PaymentMethodType.BUCKAROO_IDEAL -> R.layout.payment_method_button_ideal
            PaymentMethodType.BUCKAROO_EPS -> R.layout.payment_method_button_eps
            PaymentMethodType.BUCKAROO_BANCONTACT -> R.layout.payment_method_button_bancontact
            PaymentMethodType.XFERS_PAYNOW -> R.layout.payment_method_button_xfers
            PaymentMethodType.COINBASE -> R.layout.payment_method_button_coinbase
            PaymentMethodType.TWOC2P -> R.layout.payment_method_button_twoc2p
            else -> null
        }

        return layoutId?.let { LayoutInflater.from(context).inflate(it, null) }?.apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    context.resources.getDimensionPixelOffset(
                        R.dimen.primer_payment_method_preview_button_height
                    )
                )
            )
        }
    }
}
