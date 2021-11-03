package io.primer.android.payment.async

import io.primer.android.PaymentMethod
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.model.dto.PaymentMethodType
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodDescriptorFactory
import io.primer.android.payment.async.alipay.AlipayPaymentMethodDescriptor
import io.primer.android.payment.async.giropay.GiropayPaymentMethodDescriptor
import io.primer.android.payment.async.hoolah.HoolahPaymentMethodDescriptor
import io.primer.android.payment.async.ideal.PayNLIdealPaymentMethodDescriptor
import io.primer.android.payment.async.sofort.SofortPaymentMethodDescriptor
import io.primer.android.payment.async.trustly.TrustyPaymentMethodDescriptor
import io.primer.android.payment.async.twint.TwintPaymentMethodDescriptor
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry

internal class AsyncPaymentMethodDescriptorFactory : PaymentMethodDescriptorFactory {

    override fun create(
        localConfig: PrimerConfig,
        paymentMethodRemoteConfig: PaymentMethodRemoteConfig,
        paymentMethod: PaymentMethod,
        paymentMethodCheckers: PaymentMethodCheckerRegistry,
    ): PaymentMethodDescriptor {
        return when (paymentMethodRemoteConfig.type) {
            PaymentMethodType.PAY_NL_IDEAL -> PayNLIdealPaymentMethodDescriptor(
                localConfig,
                paymentMethod as AsyncPaymentMethod,
                paymentMethodRemoteConfig
            )
            PaymentMethodType.HOOLAH -> HoolahPaymentMethodDescriptor(
                localConfig,
                paymentMethod as AsyncPaymentMethod,
                paymentMethodRemoteConfig
            )
            PaymentMethodType.ADYEN_GIROPAY -> GiropayPaymentMethodDescriptor(
                localConfig,
                paymentMethod as AsyncPaymentMethod,
                paymentMethodRemoteConfig
            )
            PaymentMethodType.ADYEN_TWINT -> TwintPaymentMethodDescriptor(
                localConfig,
                paymentMethod as AsyncPaymentMethod,
                paymentMethodRemoteConfig
            )
            PaymentMethodType.ADYEN_SOFORT -> SofortPaymentMethodDescriptor(
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
            else -> throw IllegalStateException(
                "Unknown payment type ${paymentMethodRemoteConfig.type}"
            )
        }
    }
}
