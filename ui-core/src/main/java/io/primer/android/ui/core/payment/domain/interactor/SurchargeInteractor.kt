package io.primer.android.ui.core.payment.domain.interactor

import io.primer.android.configuration.data.extensions.surcharges
import io.primer.android.configuration.domain.model.Surcharge
import io.primer.android.configuration.domain.repository.ConfigurationRepository
import io.primer.android.core.domain.BaseInteractor
import io.primer.android.core.domain.None

class SurchargeInteractor(private val configurationRepository: ConfigurationRepository) :
    BaseInteractor<Map<String, Surcharge>, None>() {
    override fun execute(params: None): Map<String, Surcharge> {
        return configurationRepository.getConfiguration().clientSession.clientSessionDataResponse.paymentMethod
            ?.surcharges().orEmpty()
    }
}
