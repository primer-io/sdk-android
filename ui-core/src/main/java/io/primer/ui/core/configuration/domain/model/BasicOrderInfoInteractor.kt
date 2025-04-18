package io.primer.ui.core.configuration.domain.model

import io.primer.android.configuration.domain.repository.ConfigurationRepository
import io.primer.android.core.domain.BaseInteractor
import io.primer.android.core.domain.None

class BasicOrderInfoInteractor(private val configurationRepository: ConfigurationRepository) :
    BaseInteractor<BasicOrderInfo, None>() {
    override fun execute(params: None): BasicOrderInfo {
        return configurationRepository.getConfiguration().let { configuration ->
            val order = requireNotNull(configuration.clientSession.clientSessionDataResponse.order)
            BasicOrderInfo(totalAmount = order.currentAmount, currencyCode = requireNotNull(order.currencyCode))
        }
    }
}
