package io.primer.android.payment.async

import io.primer.android.PaymentMethod
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.model.dto.PaymentMethodType
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodDescriptorFactory
import io.primer.android.payment.async.alipay.AlipayPaymentMethodDescriptor
import io.primer.android.payment.async.atome.AtomePaymentMethodDescriptor
import io.primer.android.payment.async.bancontact.BancontactPaymentMethodDescriptor
import io.primer.android.payment.async.blik.AdyenBlikPaymentMethodDescriptor
import io.primer.android.payment.async.dotpay.AdyenDotpayPaymentMethodDescriptor
import io.primer.android.payment.async.eps.EpsPaymentMethodDescriptor
import io.primer.android.payment.async.giropay.GiropayPaymentMethodDescriptor
import io.primer.android.payment.async.hoolah.HoolahPaymentMethodDescriptor
import io.primer.android.payment.async.ideal.AdyenIdealPaymentMethodDescriptor
import io.primer.android.payment.async.ideal.IdealPaymentMethodDescriptor
import io.primer.android.payment.async.interac.AdyenInteracPaymentMethodDescriptor
import io.primer.android.payment.async.mbway.AdyenMbWayPaymentMethodDescriptor
import io.primer.android.payment.async.mobilepay.MobilePayPaymentMethodDescriptor
import io.primer.android.payment.async.p24.P24PaymentMethodDescriptor
import io.primer.android.payment.async.payconiq.PayconiqPaymentMethodDescriptor
import io.primer.android.payment.async.paytrail.AdyenPayTrailPaymentMethodDescriptor
import io.primer.android.payment.async.sepa.AdyenSepaPaymentMethodDescriptor
import io.primer.android.payment.async.sofort.SofortPaymentMethodDescriptor
import io.primer.android.payment.async.trustly.TrustyPaymentMethodDescriptor
import io.primer.android.payment.async.twint.TwintPaymentMethodDescriptor
import io.primer.android.payment.async.vipps.VippsPaymentMethodDescriptor
import io.primer.android.payment.async.xfers.XfersPaymentMethodDescriptor
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry

internal class AsyncPaymentMethodDescriptorFactory : PaymentMethodDescriptorFactory {

    override fun create(
        localConfig: PrimerConfig,
        paymentMethodRemoteConfig: PaymentMethodRemoteConfig,
        paymentMethod: PaymentMethod,
        paymentMethodCheckers: PaymentMethodCheckerRegistry,
    ): PaymentMethodDescriptor {
        return when (paymentMethodRemoteConfig.type) {
            PaymentMethodType.PAY_NL_IDEAL,
            PaymentMethodType.MOLLIE_IDEAL,
            PaymentMethodType.BUCKAROO_IDEAL -> IdealPaymentMethodDescriptor(
                localConfig,
                paymentMethod as AsyncPaymentMethod,
                paymentMethodRemoteConfig
            )
            PaymentMethodType.PAY_NL_PAYCONIQ -> PayconiqPaymentMethodDescriptor(
                localConfig,
                paymentMethod as AsyncPaymentMethod,
                paymentMethodRemoteConfig
            )
            PaymentMethodType.HOOLAH -> HoolahPaymentMethodDescriptor(
                localConfig,
                paymentMethod as AsyncPaymentMethod,
                paymentMethodRemoteConfig
            )
            PaymentMethodType.MOLLIE_GIROPAY,
            PaymentMethodType.ADYEN_GIROPAY,
            PaymentMethodType.PAY_NL_GIROPAY,
            PaymentMethodType.BUCKAROO_GIROPAY -> GiropayPaymentMethodDescriptor(
                localConfig,
                paymentMethod as AsyncPaymentMethod,
                paymentMethodRemoteConfig
            )
            PaymentMethodType.ADYEN_TWINT -> TwintPaymentMethodDescriptor(
                localConfig,
                paymentMethod as AsyncPaymentMethod,
                paymentMethodRemoteConfig
            )
            PaymentMethodType.ADYEN_SOFORT,
            PaymentMethodType.BUCKAROO_SOFORT -> SofortPaymentMethodDescriptor(
                localConfig,
                paymentMethod as AsyncPaymentMethod,
                paymentMethodRemoteConfig
            )
            PaymentMethodType.ADYEN_TRUSTLY -> TrustyPaymentMethodDescriptor(
                localConfig,
                paymentMethod as AsyncPaymentMethod,
                paymentMethodRemoteConfig
            )
            PaymentMethodType.ADYEN_ALIPAY -> AlipayPaymentMethodDescriptor(
                localConfig,
                paymentMethod as AsyncPaymentMethod,
                paymentMethodRemoteConfig
            )
            PaymentMethodType.ADYEN_VIPPS -> VippsPaymentMethodDescriptor(
                localConfig,
                paymentMethod as AsyncPaymentMethod,
                paymentMethodRemoteConfig
            )
            PaymentMethodType.ADYEN_MOBILEPAY -> MobilePayPaymentMethodDescriptor(
                localConfig,
                paymentMethod as AsyncPaymentMethod,
                paymentMethodRemoteConfig
            )
            PaymentMethodType.ADYEN_IDEAL -> AdyenIdealPaymentMethodDescriptor(
                localConfig,
                paymentMethod as AsyncPaymentMethod,
                paymentMethodRemoteConfig
            )
            PaymentMethodType.ADYEN_DOTPAY -> AdyenDotpayPaymentMethodDescriptor(
                localConfig,
                paymentMethod as AsyncPaymentMethod,
                paymentMethodRemoteConfig
            )
            PaymentMethodType.MOLLIE_BANCONTACT,
            PaymentMethodType.BUCKAROO_BANCONTACT -> BancontactPaymentMethodDescriptor(
                localConfig,
                paymentMethod as AsyncPaymentMethod,
                paymentMethodRemoteConfig
            )
            PaymentMethodType.ADYEN_BLIK -> AdyenBlikPaymentMethodDescriptor(
                localConfig,
                paymentMethod as AsyncPaymentMethod,
                paymentMethodRemoteConfig
            )
            PaymentMethodType.ADYEN_MBWAY -> AdyenMbWayPaymentMethodDescriptor(
                localConfig,
                paymentMethod as AsyncPaymentMethod,
                paymentMethodRemoteConfig
            )
            PaymentMethodType.ADYEN_PAYTRAIL -> AdyenPayTrailPaymentMethodDescriptor(
                localConfig,
                paymentMethod as AsyncPaymentMethod,
                paymentMethodRemoteConfig
            )
            PaymentMethodType.ADYEN_INTERAC -> AdyenInteracPaymentMethodDescriptor(
                localConfig,
                paymentMethod as AsyncPaymentMethod,
                paymentMethodRemoteConfig
            )
            PaymentMethodType.ADYEN_BANK_TRANSFER -> AdyenSepaPaymentMethodDescriptor(
                localConfig,
                paymentMethod as AsyncPaymentMethod,
                paymentMethodRemoteConfig
            )
            PaymentMethodType.MOLLIE_EPS,
            PaymentMethodType.PAY_NL_EPS,
            PaymentMethodType.BUCKAROO_EPS -> EpsPaymentMethodDescriptor(
                localConfig,
                paymentMethod as AsyncPaymentMethod,
                paymentMethodRemoteConfig
            )
            PaymentMethodType.ATOME -> AtomePaymentMethodDescriptor(
                localConfig,
                paymentMethod as AsyncPaymentMethod,
                paymentMethodRemoteConfig
            )
            PaymentMethodType.XFERS_PAYNOW -> XfersPaymentMethodDescriptor(
                localConfig,
                paymentMethod as AsyncPaymentMethod,
                paymentMethodRemoteConfig
            )
            PaymentMethodType.PAY_NL_P24,
            PaymentMethodType.MOLLIE_P24 -> P24PaymentMethodDescriptor(
                localConfig,
                paymentMethod as AsyncPaymentMethod,
                paymentMethodRemoteConfig
            )
            else -> throw IllegalStateException(
                "Unknown payment type ${paymentMethodRemoteConfig.type}"
            )
        }
    }
}
