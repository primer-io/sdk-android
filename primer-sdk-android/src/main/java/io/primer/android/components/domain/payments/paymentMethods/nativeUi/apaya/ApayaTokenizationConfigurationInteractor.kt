package io.primer.android.components.domain.payments.paymentMethods.nativeUi.apaya

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.apaya.models.ApayaTokenizationConfiguration
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.apaya.repository.ApayaTokenizationConfigurationRepository
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.domain.base.None
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.extensions.doOnError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

internal class ApayaTokenizationConfigurationInteractor(
    private val tokenizationConfigurationRepository: ApayaTokenizationConfigurationRepository,
    private val baseErrorEventResolver: BaseErrorEventResolver,
    override val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : BaseFlowInteractor<ApayaTokenizationConfiguration, None>() {
    override fun execute(params: None): Flow<ApayaTokenizationConfiguration> {
        return tokenizationConfigurationRepository.getConfiguration()
            .doOnError { baseErrorEventResolver.resolve(it, ErrorMapperType.DEFAULT) }
            .flowOn(dispatcher)
    }
}
