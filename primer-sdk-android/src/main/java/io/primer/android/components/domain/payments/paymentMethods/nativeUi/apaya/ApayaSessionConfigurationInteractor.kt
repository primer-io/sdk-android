package io.primer.android.components.domain.payments.paymentMethods.nativeUi.apaya

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.apaya.models.ApayaSessionConfiguration
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.apaya.repository.ApayaSessionConfigurationRepository
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.domain.base.None
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.extensions.doOnError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

internal class ApayaSessionConfigurationInteractor(
    private val apayaConfigurationRepository: ApayaSessionConfigurationRepository,
    private val baseErrorEventResolver: BaseErrorEventResolver,
    override val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : BaseFlowInteractor<ApayaSessionConfiguration, None>() {
    override fun execute(params: None): Flow<ApayaSessionConfiguration> {
        return apayaConfigurationRepository.getConfiguration()
            .doOnError { baseErrorEventResolver.resolve(it, ErrorMapperType.DEFAULT) }
            .flowOn(dispatcher)
    }
}
