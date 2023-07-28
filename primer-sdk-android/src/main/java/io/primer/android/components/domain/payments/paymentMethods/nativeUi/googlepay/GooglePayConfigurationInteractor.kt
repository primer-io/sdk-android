package io.primer.android.components.domain.payments.paymentMethods.nativeUi.googlepay

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.googlepay.models.GooglePayConfiguration
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.googlepay.repository.GooglePayConfigurationRepository
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.domain.base.None
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.extensions.doOnError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

internal class GooglePayConfigurationInteractor(
    private val configurationRepository: GooglePayConfigurationRepository,
    private val baseErrorEventResolver: BaseErrorEventResolver,
    override val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : BaseFlowInteractor<GooglePayConfiguration, None>() {

    override fun execute(params: None): Flow<GooglePayConfiguration> {
        return configurationRepository.getConfiguration()
            .doOnError { baseErrorEventResolver.resolve(it, ErrorMapperType.DEFAULT) }
            .flowOn(dispatcher)
    }
}
