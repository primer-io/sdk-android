package io.primer.android.data.payments.methods.repository

import android.content.Context
import io.primer.android.PaymentMethod
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.models.ConfigurationData
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.payments.methods.repository.PaymentMethodDescriptorsRepository
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.payment.PaymentMethodListFactory
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry
import io.primer.android.viewmodel.PrimerPaymentMethodDescriptorResolver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

internal class PaymentMethodDescriptorsDataRepository(
    private val context: Context,
    private val configurationDataSource: LocalConfigurationDataSource,
    private val paymentMethodCheckerRegistry: PaymentMethodCheckerRegistry,
    private val paymentMethodDescriptorFactoryRegistry: PaymentMethodDescriptorFactoryRegistry,
    private val paymentMethodListFactory: PaymentMethodListFactory,
    private val config: PrimerConfig
) : PaymentMethodDescriptorsRepository {

    private val descriptors = mutableListOf<PaymentMethodDescriptor>()

    override fun resolvePaymentMethodDescriptors(): Flow<List<PaymentMethodDescriptor>> {
        return configurationDataSource.get()
            .mapLatest { checkoutSession ->
                val paymentMethodDescriptorResolver = PrimerPaymentMethodDescriptorResolver(
                    config,
                    getPaymentMethods(checkoutSession),
                    paymentMethodDescriptorFactoryRegistry,
                    paymentMethodCheckerRegistry
                )

                paymentMethodDescriptorResolver.resolve(checkoutSession.paymentMethods).apply {
                    descriptors.addAll(this)
                }
            }
    }

    override fun getPaymentMethodDescriptors(): List<PaymentMethodDescriptor> {
        return descriptors
    }

    private fun getPaymentMethods(configuration: ConfigurationData): List<PaymentMethod> {
        val paymentMethods = paymentMethodListFactory.buildWith(configuration.paymentMethods)
        paymentMethods.forEach { paymentMethod ->
            initializeAndRegisterModules(context, paymentMethod, configuration)
        }

        return paymentMethods
    }

    private fun initializeAndRegisterModules(
        context: Context,
        paymentMethod: PaymentMethod,
        configuration: ConfigurationData
    ) {
        if (config.paymentMethodIntent.isNotVault ||
            (config.paymentMethodIntent.isVault && paymentMethod.canBeVaulted) ||
            config.settings.fromHUC
        ) {
            paymentMethod.module.initialize(context, configuration)
            paymentMethod.module.registerPaymentMethodCheckers(paymentMethodCheckerRegistry)
            paymentMethod.module.registerPaymentMethodDescriptorFactory(
                paymentMethodDescriptorFactoryRegistry
            )
        }
    }
}
