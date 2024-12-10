package io.primer.android.clientSessionActions.domain.validator

import io.primer.android.clientSessionActions.domain.models.ActionUpdateBillingAddressParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateCustomerDetailsParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateEmailAddressParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateMobileNumberParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateShippingAddressParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateShippingOptionIdParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateUnselectPaymentMethodParams
import io.primer.android.clientSessionActions.domain.models.BaseActionUpdateParams
import io.primer.android.configuration.domain.repository.ConfigurationRepository
import io.primer.android.data.settings.internal.PrimerConfig

internal class ActionUpdateFilter(
    private val configurationRepository: ConfigurationRepository,
    private val config: PrimerConfig
) {

    fun filter(updateParams: BaseActionUpdateParams): Boolean {
        return when (updateParams) {
            is ActionUpdateSelectPaymentMethodParams,
            is ActionUpdateUnselectPaymentMethodParams ->
                configurationRepository.getConfiguration().let {
                    config.intent.paymentMethodIntent.isVault ||
                        it.clientSession.clientSessionDataResponse
                            .paymentMethod
                            ?.surcharges.orEmpty()
                            .all { item -> item.value == 0 } // TODO: only consider currently available payment methods as an optimization
                }

            is ActionUpdateBillingAddressParams,
            is ActionUpdateCustomerDetailsParams,
            is ActionUpdateMobileNumberParams,
            is ActionUpdateShippingAddressParams,
            is ActionUpdateShippingOptionIdParams,
            is ActionUpdateEmailAddressParams -> false
        }
    }
}
