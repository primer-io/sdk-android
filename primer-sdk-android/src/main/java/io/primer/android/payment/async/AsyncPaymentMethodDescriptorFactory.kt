package io.primer.android.payment.async

import io.primer.android.PaymentMethod
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.configuration.models.PaymentMethodImplementationType
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodDescriptorFactory
import io.primer.android.payment.async.blik.AdyenBlikPaymentMethodDescriptor
import io.primer.android.payment.async.dotpay.AdyenDotpayPaymentMethodDescriptor
import io.primer.android.payment.async.fastbanktransfer.RapydFastPaymentMethodDescriptor
import io.primer.android.payment.async.ideal.AdyenIdealPaymentMethodDescriptor
import io.primer.android.payment.async.mbway.AdyenMbWayPaymentMethodDescriptor
import io.primer.android.payment.async.multibanco.AdyenMultibancoPaymentMethodDescriptor
import io.primer.android.payment.async.ovo.XenditOvoPaymentMethodDescriptor
import io.primer.android.payment.async.promptpay.OmisePromptPayPaymentMethodDescriptor
import io.primer.android.payment.async.promptpay.RapydPromptPayPaymentMethodDescriptor
import io.primer.android.payment.async.sepa.AdyenSepaPaymentMethodDescriptor
import io.primer.android.payment.async.sofort.PrimerTestSofortPaymentMethodDescriptor
import io.primer.android.payment.async.webRedirect.WebRedirectPaymentMethodDescriptor
import io.primer.android.payment.async.xfers.XfersPaymentMethodDescriptor
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry

internal class AsyncPaymentMethodDescriptorFactory : PaymentMethodDescriptorFactory {

    override fun create(
        localConfig: PrimerConfig,
        paymentMethodRemoteConfig: PaymentMethodConfigDataResponse,
        paymentMethod: PaymentMethod,
        paymentMethodCheckers: PaymentMethodCheckerRegistry,
    ): PaymentMethodDescriptor {
        return when (paymentMethodRemoteConfig.implementationType) {
            PaymentMethodImplementationType.NATIVE_SDK -> when (
                PaymentMethodType.safeValueOf(paymentMethodRemoteConfig.type)
            ) {
                PaymentMethodType.PRIMER_TEST_SOFORT ->
                    PrimerTestSofortPaymentMethodDescriptor(
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
                PaymentMethodType.ADYEN_BANK_TRANSFER -> AdyenSepaPaymentMethodDescriptor(
                    localConfig,
                    paymentMethod as AsyncPaymentMethod,
                    paymentMethodRemoteConfig
                )
                PaymentMethodType.XFERS_PAYNOW -> XfersPaymentMethodDescriptor(
                    localConfig,
                    paymentMethod as AsyncPaymentMethod,
                    paymentMethodRemoteConfig
                )
                PaymentMethodType.RAPYD_FAST -> RapydFastPaymentMethodDescriptor(
                    localConfig,
                    paymentMethod as AsyncPaymentMethod,
                    paymentMethodRemoteConfig
                )
                PaymentMethodType.RAPYD_PROMPTPAY -> RapydPromptPayPaymentMethodDescriptor(
                    localConfig,
                    paymentMethod as AsyncPaymentMethod,
                    paymentMethodRemoteConfig
                )
                PaymentMethodType.OMISE_PROMPTPAY -> OmisePromptPayPaymentMethodDescriptor(
                    localConfig,
                    paymentMethod as AsyncPaymentMethod,
                    paymentMethodRemoteConfig
                )
                PaymentMethodType.XENDIT_OVO -> XenditOvoPaymentMethodDescriptor(
                    localConfig,
                    paymentMethod as AsyncPaymentMethod,
                    paymentMethodRemoteConfig
                )
                PaymentMethodType.ADYEN_MULTIBANCO -> AdyenMultibancoPaymentMethodDescriptor(
                    localConfig,
                    paymentMethod as AsyncPaymentMethod,
                    paymentMethodRemoteConfig
                )
                else -> throw IllegalStateException(
                    "Unknown payment type ${paymentMethodRemoteConfig.type}"
                )
            }
            PaymentMethodImplementationType.WEB_REDIRECT ->
                return WebRedirectPaymentMethodDescriptor(
                    localConfig,
                    paymentMethod as AsyncPaymentMethod,
                    paymentMethodRemoteConfig
                )
            PaymentMethodImplementationType.UNKNOWN -> throw IllegalStateException(
                "Unknown payment type ${paymentMethodRemoteConfig.type}"
            )
        }
    }
}
