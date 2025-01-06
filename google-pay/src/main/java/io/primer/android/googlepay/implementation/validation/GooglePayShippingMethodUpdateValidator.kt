package io.primer.android.googlepay.implementation.validation

import io.primer.android.clientSessionActions.domain.models.ActionUpdateShippingOptionIdParams
import io.primer.android.configuration.domain.model.CheckoutModule
import io.primer.android.googlepay.implementation.configuration.domain.GooglePayConfigurationRepository
import io.primer.android.googlepay.implementation.errors.domain.exception.ShippingAddressUnserviceableException
import io.primer.android.paymentmethods.core.configuration.domain.model.NoOpPaymentMethodConfigurationParams

/**
 * Validates the shipping address is still serviceable by the merchant,
 * after a potential address or payment method update on the GooglePay sheet.
 */
internal class GooglePayShippingMethodUpdateValidator(
    private val configurationRepository: GooglePayConfigurationRepository,
) {
    operator fun invoke(shippingOptionId: ActionUpdateShippingOptionIdParams): Result<Unit> {
        return configurationRepository.getPaymentMethodConfiguration(NoOpPaymentMethodConfigurationParams)
            .map { configuration ->
                if (!isSelectedShippingMethodAvailable(configuration.shippingOptions, shippingOptionId)) {
                    throw ShippingAddressUnserviceableException(shippingOptionId.id)
                }
                return@map
            }
    }

    private fun isSelectedShippingMethodAvailable(
        shippingOptions: CheckoutModule.Shipping?,
        params: ActionUpdateShippingOptionIdParams?,
    ): Boolean {
        val availableShippingMethod = shippingOptions?.shippingMethods?.firstOrNull { it.id == params?.id }
        return availableShippingMethod != null
    }
}
