package io.primer.android.data.payments.methods.repository

import android.content.Context
import io.primer.android.PaymentMethod
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.domain.payments.methods.repository.PaymentMethodsRepository
import io.primer.android.data.configuration.model.Configuration
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.model.dto.PrimerPaymentMethod
import io.primer.android.payment.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.payment.PaymentMethodListFactory
import io.primer.android.payment.apaya.Apaya
import io.primer.android.payment.async.AsyncPaymentMethod
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
                val matches: Boolean = when (config.intent.paymentMethod) {
                    PrimerPaymentMethod.KLARNA -> p is Klarna
                    PrimerPaymentMethod.GOOGLE_PAY -> p is GooglePay
                    PrimerPaymentMethod.PAYPAL -> p is PayPal
                    PrimerPaymentMethod.CARD -> p is Card
                    PrimerPaymentMethod.GOCARDLESS -> p is GoCardless
                    PrimerPaymentMethod.APAYA -> p is Apaya
                    PrimerPaymentMethod.ASYNC -> p is AsyncPaymentMethod
                    else -> false
                }
                matches
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
