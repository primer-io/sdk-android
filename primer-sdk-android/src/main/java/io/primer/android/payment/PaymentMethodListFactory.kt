package io.primer.android.payment

import io.primer.android.PaymentMethod
import io.primer.android.logging.DefaultLogger
import io.primer.android.logging.Logger
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.utils.Failure
import io.primer.android.utils.Success

internal class PaymentMethodListFactory(
    private val mapping: PaymentMethodMapping,
    private val logger: Logger = DefaultLogger(TAG)
) {

    fun buildWith(configList: List<PaymentMethodRemoteConfig>): MutableList<PaymentMethod> {

        val paymentMethods = mutableListOf<PaymentMethod>()

        configList.forEach { config ->
            when (val result = mapping.getPaymentMethodFor(config.type)) {
                is Success -> paymentMethods.add(result.value)
                is Failure -> logger.warn(
                    "Invalid config for ${config.type} ${result.value.message.orEmpty()}"
                )
            }
        }

        return paymentMethods
    }

    private companion object {
        const val TAG = "PaymentMethodListFactory"
    }
}
