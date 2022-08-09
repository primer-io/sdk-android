package io.primer.android.data.payments.methods.repository

import android.content.Context
import io.primer.android.PaymentMethod
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.models.ConfigurationData
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.payments.methods.repository.PaymentMethodsRepository
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

    private fun getPaymentMethods(configuration: ConfigurationData): List<PaymentMethod> {
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
        PaymentMethodType.KLARNA.name -> p is Klarna
        PaymentMethodType.GOOGLE_PAY.name -> p is GooglePay
        PaymentMethodType.PAYPAL.name -> p is PayPal
        PaymentMethodType.PAYMENT_CARD.name -> p is Card
        PaymentMethodType.GOCARDLESS.name -> p is GoCardless
        PaymentMethodType.APAYA.name -> p is Apaya
        else -> p.type == config.intent.paymentMethod
    }

    private fun initializeAndRegisterModules(
        context: Context,
        paymentMethod: PaymentMethod,
        configuration: ConfigurationData
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
