package io.primer.android.surcharge.domain

import io.primer.android.configuration.domain.model.Surcharge
import io.primer.android.configuration.domain.repository.ConfigurationRepository
import io.primer.android.core.domain.BaseInteractor
import io.primer.android.core.domain.None
import io.primer.android.ui.core.payment.domain.interactor.SurchargeInteractor as UiCoreSurchargeInteractor

internal class SurchargeInteractor(private val configurationRepository: ConfigurationRepository) :
    BaseInteractor<Map<String, Surcharge>, None>() {
    
    private val uiCoreSurchargeInteractor = UiCoreSurchargeInteractor(configurationRepository)
    
    override fun execute(params: None): Map<String, Surcharge> {
        return uiCoreSurchargeInteractor.execute(params)
    }
}
