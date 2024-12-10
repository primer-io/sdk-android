package io.primer.android.components

import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.core.utils.Failure
import io.primer.android.paymentmethods.PaymentMethod
import io.primer.android.core.utils.Success

internal class PaymentMethodListFactory(
    private val mapping: PaymentMethodMapping,
    private val logReporter: LogReporter
) {

    fun buildWith(configList: List<PaymentMethodConfigDataResponse>): MutableList<PaymentMethod> {
        val paymentMethods = mutableListOf<PaymentMethod>()

        configList.forEach { config ->
            when (
                val result = mapping.getPaymentMethodFor(config.implementationType, config.type)
            ) {
                is Success -> paymentMethods.add(result.value)
                is Failure -> logReporter.warn(
                    "${config.type} will be filtered due to invalid config." +
                        " ${result.value.message.orEmpty()}",
                    PAYMENT_METHODS_BUILDER_LOGGING_COMPONENT
                )
            }
        }

        return paymentMethods
    }

    private companion object {

        const val PAYMENT_METHODS_BUILDER_LOGGING_COMPONENT = "Payment Method Builder"
    }
}
