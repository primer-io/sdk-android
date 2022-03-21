package io.primer.android.data.payments.methods.repository

import android.content.Context
import io.primer.android.PaymentMethod
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.model.Configuration
import io.primer.android.domain.payments.methods.repository.PaymentMethodsRepository
import io.primer.android.model.dto.PaymentMethodType
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.model.dto.PrimerPaymentMethod
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
                isMatchMethods(p)
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

    private fun isMatchMethods(p: PaymentMethod): Boolean = when (config.intent.paymentMethod) {
        PrimerPaymentMethod.KLARNA -> p is Klarna
        PrimerPaymentMethod.GOOGLE_PAY -> p is GooglePay
        PrimerPaymentMethod.PAYPAL -> p is PayPal
        PrimerPaymentMethod.CARD -> p is Card
        PrimerPaymentMethod.GOCARDLESS -> p is GoCardless
        PrimerPaymentMethod.APAYA -> p is Apaya
        PrimerPaymentMethod.ATOME -> p.type == PaymentMethodType.ATOME
        PrimerPaymentMethod.PAY_NL_IDEAL -> p.type == PaymentMethodType.PAY_NL_IDEAL
        PrimerPaymentMethod.PAY_NL_PAYCONIQ -> p.type == PaymentMethodType.PAY_NL_PAYCONIQ
        PrimerPaymentMethod.PAY_NL_GIROPAY -> p.type == PaymentMethodType.PAY_NL_GIROPAY
        PrimerPaymentMethod.HOOLAH -> p.type == PaymentMethodType.HOOLAH
        PrimerPaymentMethod.ADYEN_GIROPAY -> p.type == PaymentMethodType.ADYEN_GIROPAY
        PrimerPaymentMethod.ADYEN_TWINT -> p.type == PaymentMethodType.ADYEN_TWINT
        PrimerPaymentMethod.ADYEN_SOFORT -> p.type == PaymentMethodType.ADYEN_SOFORT
        PrimerPaymentMethod.ADYEN_TRUSTLY -> p.type == PaymentMethodType.ADYEN_TRUSTLY
        PrimerPaymentMethod.ADYEN_ALIPAY -> p.type == PaymentMethodType.ADYEN_ALIPAY
        PrimerPaymentMethod.ADYEN_VIPPS -> p.type == PaymentMethodType.ADYEN_VIPPS
        PrimerPaymentMethod.ADYEN_MOBILEPAY -> p.type == PaymentMethodType.ADYEN_MOBILEPAY
        PrimerPaymentMethod.ADYEN_INTERAC -> p.type == PaymentMethodType.ADYEN_INTERAC
        PrimerPaymentMethod.ADYEN_PAYTRAIL -> p.type == PaymentMethodType.ADYEN_PAYTRAIL
        PrimerPaymentMethod.ADYEN_PAYSHOP -> p.type == PaymentMethodType.ADYEN_PAYSHOP
        PrimerPaymentMethod.MOLLIE_BANCONTACT -> p.type == PaymentMethodType.MOLLIE_BANCONTACT
        PrimerPaymentMethod.MOLLIE_IDEAL -> p.type == PaymentMethodType.MOLLIE_IDEAL
        PrimerPaymentMethod.BUCKAROO_GIROPAY -> p.type == PaymentMethodType.BUCKAROO_GIROPAY
        PrimerPaymentMethod.BUCKAROO_SOFORT -> p.type == PaymentMethodType.BUCKAROO_SOFORT
        PrimerPaymentMethod.BUCKAROO_IDEAL -> p.type == PaymentMethodType.BUCKAROO_IDEAL
        PrimerPaymentMethod.BUCKAROO_EPS -> p.type == PaymentMethodType.BUCKAROO_EPS
        PrimerPaymentMethod.BUCKAROO_BANCONTACT -> p.type == PaymentMethodType.BUCKAROO_BANCONTACT
        else -> false
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
