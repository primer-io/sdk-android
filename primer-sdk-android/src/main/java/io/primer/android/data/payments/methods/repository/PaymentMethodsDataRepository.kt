package io.primer.android.data.payments.methods.repository

import android.content.Context
import io.primer.android.PaymentMethod
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.models.Configuration
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.configuration.models.PrimerPaymentMethodType
import io.primer.android.domain.payments.methods.repository.PaymentMethodsRepository
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.payment.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.payment.PaymentMethodListFactory
import io.primer.android.payment.apaya.Apaya
import io.primer.android.payment.card.Card
import io.primer.android.payment.gocardless.GoCardless
import io.primer.android.payment.google.GooglePay
import io.primer.android.payment.klarna.Klarna
import io.primer.android.payment.paypal.PayPal
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry
import io.primer.android.viewmodel.PrimerPaymentMethodDescriptorResolver
import kotlinx.coroutines.flow.mapLatest

internal class PaymentMethodsDataRepository(
    private val context: Context,
    private val configurationDataSource: LocalConfigurationDataSource,
    private val paymentMethodCheckerRegistry: PaymentMethodCheckerRegistry,
    private val paymentMethodDescriptorFactoryRegistry: PaymentMethodDescriptorFactoryRegistry,
    private val paymentMethodListFactory: PaymentMethodListFactory,
    private val config: PrimerConfig
) : PaymentMethodsRepository {

    override fun getPaymentMethodDescriptors() =
        configurationDataSource.get()
            .mapLatest { checkoutSession ->
                val paymentMethodDescriptorResolver = PrimerPaymentMethodDescriptorResolver(
                    config,
                    getPaymentMethods(checkoutSession),
                    paymentMethodDescriptorFactoryRegistry,
                    paymentMethodCheckerRegistry
                )

                paymentMethodDescriptorResolver.resolve(checkoutSession.paymentMethods)
            }

    private fun getPaymentMethods(configuration: Configuration): List<PaymentMethod> {
        val paymentMethods = paymentMethodListFactory.buildWith(configuration.paymentMethods)
        if (config.isStandalonePaymentMethod) {
            paymentMethods.find { p ->
                matchesPaymentMethod(p)
            }?.let { paymentMethod ->
                initializeAndRegisterModules(context, paymentMethod, configuration)
            }
        } else {
            paymentMethods.forEach { paymentMethod ->
                initializeAndRegisterModules(context, paymentMethod, configuration)
            }
        }

        return paymentMethods
    }

    private fun matchesPaymentMethod(p: PaymentMethod) = when (config.intent.paymentMethod) {
        PrimerPaymentMethodType.KLARNA -> p is Klarna
        PrimerPaymentMethodType.GOOGLE_PAY -> p is GooglePay
        PrimerPaymentMethodType.PAYPAL -> p is PayPal
        PrimerPaymentMethodType.PAYMENT_CARD -> p is Card
        PrimerPaymentMethodType.GOCARDLESS -> p is GoCardless
        PrimerPaymentMethodType.APAYA -> p is Apaya
        PrimerPaymentMethodType.ATOME -> p.type == PaymentMethodType.ATOME
        PrimerPaymentMethodType.PAY_NL_IDEAL -> p.type == PaymentMethodType.PAY_NL_IDEAL
        PrimerPaymentMethodType.PAY_NL_PAYCONIQ -> p.type == PaymentMethodType.PAY_NL_PAYCONIQ
        PrimerPaymentMethodType.PAY_NL_GIROPAY -> p.type == PaymentMethodType.PAY_NL_GIROPAY
        PrimerPaymentMethodType.HOOLAH -> p.type == PaymentMethodType.HOOLAH
        PrimerPaymentMethodType.ADYEN_GIROPAY -> p.type == PaymentMethodType.ADYEN_GIROPAY
        PrimerPaymentMethodType.ADYEN_TWINT -> p.type == PaymentMethodType.ADYEN_TWINT
        PrimerPaymentMethodType.ADYEN_SOFORT -> p.type == PaymentMethodType.ADYEN_SOFORT
        PrimerPaymentMethodType.ADYEN_TRUSTLY -> p.type == PaymentMethodType.ADYEN_TRUSTLY
        PrimerPaymentMethodType.ADYEN_ALIPAY -> p.type == PaymentMethodType.ADYEN_ALIPAY
        PrimerPaymentMethodType.ADYEN_VIPPS -> p.type == PaymentMethodType.ADYEN_VIPPS
        PrimerPaymentMethodType.ADYEN_MOBILEPAY -> p.type == PaymentMethodType.ADYEN_MOBILEPAY
        PrimerPaymentMethodType.ADYEN_PAYTRAIL -> p.type == PaymentMethodType.ADYEN_PAYTRAIL
        PrimerPaymentMethodType.ADYEN_INTERAC -> p.type == PaymentMethodType.ADYEN_INTERAC
        PaymentMethodType.ADYEN_PAYSHOP -> p.type == PaymentMethodType.ADYEN_PAYSHOP
        PrimerPaymentMethodType.MOLLIE_BANCONTACT -> p.type == PaymentMethodType.MOLLIE_BANCONTACT
        PrimerPaymentMethodType.MOLLIE_IDEAL -> p.type == PaymentMethodType.MOLLIE_IDEAL
        PaymentMethodType.MOLLIE_P24 -> p.type == PaymentMethodType.MOLLIE_P24
        PaymentMethodType.MOLLIE_GIROPAY -> p.type == PaymentMethodType.MOLLIE_GIROPAY
        PaymentMethodType.MOLLIE_EPS -> p.type == PaymentMethodType.MOLLIE_EPS
        PrimerPaymentMethodType.BUCKAROO_GIROPAY -> p.type == PaymentMethodType.BUCKAROO_GIROPAY
        PrimerPaymentMethodType.BUCKAROO_SOFORT -> p.type == PaymentMethodType.BUCKAROO_SOFORT
        PrimerPaymentMethodType.BUCKAROO_IDEAL -> p.type == PaymentMethodType.BUCKAROO_IDEAL
        PrimerPaymentMethodType.BUCKAROO_EPS -> p.type == PaymentMethodType.BUCKAROO_EPS
        PrimerPaymentMethodType.BUCKAROO_BANCONTACT ->
            p.type == PaymentMethodType.BUCKAROO_BANCONTACT
        PaymentMethodType.PAY_NL_P24 -> p.type == PaymentMethodType.PAY_NL_P24
        PaymentMethodType.PAY_NL_EPS -> p.type == PaymentMethodType.PAY_NL_EPS
        PaymentMethodType.ADYEN_IDEAL -> p.type == PaymentMethodType.ADYEN_IDEAL
        PaymentMethodType.ADYEN_DOTPAY -> p.type == PaymentMethodType.ADYEN_DOTPAY
        PaymentMethodType.ADYEN_BLIK -> p.type == PaymentMethodType.ADYEN_BLIK
        PaymentMethodType.ADYEN_MBWAY -> p.type == PaymentMethodType.ADYEN_MBWAY
        PaymentMethodType.ADYEN_BANK_TRANSFER -> p.type == PaymentMethodType.ADYEN_BANK_TRANSFER
        PaymentMethodType.XFERS_PAYNOW -> p.type == PaymentMethodType.XFERS_PAYNOW
        PaymentMethodType.COINBASE -> p.type == PaymentMethodType.COINBASE
        PaymentMethodType.TWOC2P -> p.type == PaymentMethodType.TWOC2P
        PaymentMethodType.OPENNODE -> p.type == PaymentMethodType.OPENNODE
        PaymentMethodType.PRIMER_TEST_KLARNA -> p.type == PaymentMethodType.PRIMER_TEST_KLARNA
        PaymentMethodType.PRIMER_TEST_PAYPAL -> p.type == PaymentMethodType.PRIMER_TEST_PAYPAL
        PaymentMethodType.PRIMER_TEST_SOFORT -> p.type == PaymentMethodType.PRIMER_TEST_SOFORT
        PaymentMethodType.RAPYD_GCASH -> p.type == PaymentMethodType.RAPYD_GCASH
        PaymentMethodType.UNKNOWN -> false
        null -> false
    }

    private fun initializeAndRegisterModules(
        context: Context,
        paymentMethod: PaymentMethod,
        configuration: Configuration
    ) {
        if (config.paymentMethodIntent.isNotVault ||
            (config.paymentMethodIntent.isVault && paymentMethod.canBeVaulted)
        ) {
            paymentMethod.module.initialize(context, configuration)
            paymentMethod.module.registerPaymentMethodCheckers(paymentMethodCheckerRegistry)
            paymentMethod.module.registerPaymentMethodDescriptorFactory(
                paymentMethodDescriptorFactoryRegistry
            )
        }
    }
}
