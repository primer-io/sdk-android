package io.primer.android.domain.action.validator

import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.action.models.ActionUpdateBillingAddressParams
import io.primer.android.domain.action.models.ActionUpdateCustomerDetailsParams
import io.primer.android.domain.action.models.ActionUpdateEmailAddressParams
import io.primer.android.domain.action.models.ActionUpdateMobileNumberParams
import io.primer.android.domain.action.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.domain.action.models.ActionUpdateShippingAddressParams
import io.primer.android.domain.action.models.ActionUpdateShippingOptionIdParams
import io.primer.android.domain.action.models.ActionUpdateUnselectPaymentMethodParams
import io.primer.android.domain.action.models.BaseActionUpdateParams
import io.primer.android.domain.session.CachePolicy
import io.primer.android.domain.session.repository.ConfigurationRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

internal class ActionUpdateFilter(
    private val configurationRepository: ConfigurationRepository,
    private val config: PrimerConfig
) {

    suspend fun filter(updateParams: BaseActionUpdateParams): Boolean {
        return when (updateParams) {
            is ActionUpdateSelectPaymentMethodParams,
            is ActionUpdateUnselectPaymentMethodParams ->
                configurationRepository.fetchConfiguration(CachePolicy.ForceCache).map {
                    config.intent.paymentMethodIntent.isVault ||
                        it.clientSession.clientSessionDataResponse
                            .paymentMethod
                            ?.surcharges.orEmpty()
                            .all { item -> item.value == 0 }
                }.first()
            is ActionUpdateBillingAddressParams,
            is ActionUpdateCustomerDetailsParams,
            is ActionUpdateMobileNumberParams,
            is ActionUpdateShippingAddressParams,
            is ActionUpdateShippingOptionIdParams,
            is ActionUpdateEmailAddressParams -> false
        }
    }
}
