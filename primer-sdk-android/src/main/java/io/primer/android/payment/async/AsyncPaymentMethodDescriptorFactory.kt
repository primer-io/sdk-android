package io.primer.android.payment.async

import io.primer.android.PaymentMethod
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.configuration.models.PaymentMethodImplementationType
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodDescriptorFactory
import io.primer.android.payment.async.bancontact.AdyenBancontactCardPaymentMethodDescriptor
import io.primer.android.payment.async.blik.AdyenBlikPaymentMethodDescriptor
import io.primer.android.payment.async.dotpay.AdyenDotpayPaymentMethodDescriptor
import io.primer.android.payment.async.fastbanktransfer.RapydFastPaymentMethodDescriptor
import io.primer.android.payment.async.ideal.AdyenIdealPaymentMethodDescriptor
import io.primer.android.payment.async.ipay88.IPay88PaymentMethodDescriptor
import io.primer.android.payment.async.mbway.AdyenMbWayPaymentMethodDescriptor
import io.primer.android.payment.async.multibanco.AdyenMultibancoPaymentMethodDescriptor
import io.primer.android.payment.async.ovo.XenditOvoPaymentMethodDescriptor
import io.primer.android.payment.async.promptpay.OmisePromptPayPaymentMethodDescriptor
import io.primer.android.payment.async.promptpay.RapydPromptPayPaymentMethodDescriptor
import io.primer.android.payment.async.retailOutlet.XenditRetailOutletPaymentMethodDescriptor
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
                        paymentMethod as AsyncPaymentMethod,
                        localConfig,
                        paymentMethodRemoteConfig
                    )
                PaymentMethodType.ADYEN_IDEAL -> AdyenIdealPaymentMethodDescriptor(
                    paymentMethod as AsyncPaymentMethod,
                    localConfig,
                    paymentMethodRemoteConfig
                )
                PaymentMethodType.ADYEN_DOTPAY -> AdyenDotpayPaymentMethodDescriptor(
                    paymentMethod as AsyncPaymentMethod,
                    localConfig,
                    paymentMethodRemoteConfig
                )
                PaymentMethodType.ADYEN_BLIK -> AdyenBlikPaymentMethodDescriptor(
                    paymentMethod as AsyncPaymentMethod,
                    localConfig,
                    paymentMethodRemoteConfig
                )
                PaymentMethodType.ADYEN_MBWAY -> AdyenMbWayPaymentMethodDescriptor(
                    paymentMethod as AsyncPaymentMethod,
                    localConfig,
                    paymentMethodRemoteConfig
                )
                PaymentMethodType.ADYEN_BANK_TRANSFER -> AdyenSepaPaymentMethodDescriptor(
                    paymentMethod as AsyncPaymentMethod,
                    localConfig,
                    paymentMethodRemoteConfig
                )
                PaymentMethodType.XFERS_PAYNOW -> XfersPaymentMethodDescriptor(
                    paymentMethod as AsyncPaymentMethod,
                    localConfig,
                    paymentMethodRemoteConfig
                )
                PaymentMethodType.RAPYD_FAST -> RapydFastPaymentMethodDescriptor(
                    paymentMethod as AsyncPaymentMethod,
                    localConfig,
                    paymentMethodRemoteConfig
                )
                PaymentMethodType.RAPYD_PROMPTPAY -> RapydPromptPayPaymentMethodDescriptor(
                    paymentMethod as AsyncPaymentMethod,
                    localConfig,
                    paymentMethodRemoteConfig
                )
                PaymentMethodType.OMISE_PROMPTPAY -> OmisePromptPayPaymentMethodDescriptor(
                    paymentMethod as AsyncPaymentMethod,
                    localConfig,
                    paymentMethodRemoteConfig
                )
                PaymentMethodType.XENDIT_OVO -> XenditOvoPaymentMethodDescriptor(
                    paymentMethod as AsyncPaymentMethod,
                    localConfig,
                    paymentMethodRemoteConfig
                )
                PaymentMethodType.ADYEN_MULTIBANCO -> AdyenMultibancoPaymentMethodDescriptor(
                    paymentMethod as AsyncPaymentMethod,
                    localConfig,
                    paymentMethodRemoteConfig
                )
                PaymentMethodType.ADYEN_BANCONTACT_CARD ->
                    AdyenBancontactCardPaymentMethodDescriptor(
                        paymentMethod as AsyncPaymentMethod,
                        localConfig,
                        paymentMethodRemoteConfig
                    )
                PaymentMethodType.XENDIT_RETAIL_OUTLETS ->
                    XenditRetailOutletPaymentMethodDescriptor(
                        paymentMethod as AsyncPaymentMethod,
                        localConfig,
                        paymentMethodRemoteConfig
                    )
                else -> throw IllegalStateException(
                    "Unknown payment type ${paymentMethodRemoteConfig.type}"
                )
            }
            PaymentMethodImplementationType.WEB_REDIRECT ->
                return WebRedirectPaymentMethodDescriptor(
                    paymentMethod as AsyncPaymentMethod,
                    localConfig,
                    paymentMethodRemoteConfig
                )
            PaymentMethodImplementationType.IPAY88_SDK ->
                return IPay88PaymentMethodDescriptor(
                    paymentMethod as AsyncPaymentMethod,
                    localConfig,
                    paymentMethodRemoteConfig
                )
            PaymentMethodImplementationType.UNKNOWN -> throw IllegalStateException(
                "Unknown payment type ${paymentMethodRemoteConfig.type}"
            )
        }
    }
}
