package io.primer.android.domain.action.validator

import io.primer.android.domain.action.models.ActionUpdateBillingAddressParams
import io.primer.android.domain.action.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.domain.action.models.ActionUpdateUnselectPaymentMethodParams
import io.primer.android.domain.action.models.BaseActionUpdateParams
import io.primer.android.domain.session.repository.ConfigurationRepository
import io.primer.android.data.settings.internal.PrimerConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal class ActionUpdateFilter(
    private val configurationRepository: ConfigurationRepository,
    private val config: PrimerConfig
) {

    fun filter(updateParams: BaseActionUpdateParams): Flow<Boolean> {
        return when (updateParams) {
            is ActionUpdateSelectPaymentMethodParams,
            is ActionUpdateUnselectPaymentMethodParams ->
                configurationRepository.fetchConfiguration(true).map {
                    config.intent.paymentMethodIntent.isVault ||
                        it.clientSession.clientSessionDataResponse
                            .paymentMethod
                            ?.surcharges.orEmpty()
                            .all { item -> item.value == 0 }
                }
            is ActionUpdateBillingAddressParams -> flowOf(false)
        }
    }
}
