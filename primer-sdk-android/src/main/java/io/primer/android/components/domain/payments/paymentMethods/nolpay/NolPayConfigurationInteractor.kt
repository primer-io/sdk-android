package io.primer.android.components.domain.payments.paymentMethods.nolpay

import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayConfiguration
import io.primer.android.components.domain.payments.paymentMethods.nolpay.repository.NolPayConfigurationRepository
import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.domain.base.None
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

internal class NolPayConfigurationInteractor(
    private val configurationRepository: NolPayConfigurationRepository,
    override val dispatcher: CoroutineDispatcher = Dispatchers.Default
) :
    BaseFlowInteractor<NolPayConfiguration, None>() {
    override fun execute(params: None): Flow<NolPayConfiguration> {
        return configurationRepository.getConfiguration().flowOn(dispatcher)
    }
}
