package io.primer.android.components.domain.payments.paymentMethods.nativeUi.googlepay

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.googlepay.repository.GooglePayConfigurationRepository
import io.primer.android.domain.action.models.ActionUpdateShippingOptionIdParams
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.exception.ShippingAddressUnserviceableException
import io.primer.android.domain.session.models.CheckoutModule
import io.primer.android.extensions.doOnError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * Validates the shipping address is still serviceable by the merchant,
 * after a potential address or payment method update on the GooglePay sheet.
 */
internal class GooglePayShippingMethodUpdateValidator(
    private val configurationRepository: GooglePayConfigurationRepository,
    private val errorResolver: BaseErrorEventResolver,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    operator fun invoke(shippingOptionId: ActionUpdateShippingOptionIdParams): Flow<Unit> {
        return configurationRepository.getConfiguration()
            .map { configuration ->
                if (!isSelectedShippingMethodAvailable(configuration.shippingOptions, shippingOptionId)) {
                    throw ShippingAddressUnserviceableException(shippingOptionId.id)
                }
                return@map
            }
            .doOnError(dispatcher) {
                errorResolver.resolve(it, ErrorMapperType.GOOGLE_PAY)
            }
            .flowOn(dispatcher)
    }

    private fun isSelectedShippingMethodAvailable(
        shippingOptions: CheckoutModule.Shipping?,
        params: ActionUpdateShippingOptionIdParams?
    ): Boolean {
        val availableShippingMethod = shippingOptions?.shippingMethods?.firstOrNull { it.id == params?.id }
        return availableShippingMethod != null
    }
}
