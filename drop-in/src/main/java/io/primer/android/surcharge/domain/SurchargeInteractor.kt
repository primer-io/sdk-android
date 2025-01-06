package io.primer.android.surcharge.domain

import io.primer.android.configuration.domain.repository.ConfigurationRepository
import io.primer.android.core.domain.BaseInteractor
import io.primer.android.core.domain.None

internal class SurchargeInteractor(private val configurationRepository: ConfigurationRepository) :
    BaseInteractor<Map<String, Int>, None>() {
    override fun execute(params: None): Map<String, Int> {
        return configurationRepository.getConfiguration().clientSession.clientSessionDataResponse.paymentMethod
            ?.surcharges.orEmpty()
    }
}
