package io.primer.android.domain.mock

import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.domain.base.Params
import io.primer.android.domain.mock.repository.MockConfigurationRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

internal class FinaliseMockedFlowInteractor(
    private val mockConfigurationRepository: MockConfigurationRepository,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseFlowInteractor<Unit, Params>() {
    override fun execute(params: Params) = mockConfigurationRepository.finalizeMockedFlow()
        .flowOn(dispatcher)
}
