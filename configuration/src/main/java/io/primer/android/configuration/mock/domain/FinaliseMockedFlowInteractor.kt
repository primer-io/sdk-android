package io.primer.android.configuration.mock.domain

import io.primer.android.configuration.mock.domain.repository.MockConfigurationRepository
import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.core.domain.Params
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class FinaliseMockedFlowInteractor internal constructor(
    private val mockConfigurationRepository: MockConfigurationRepository,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseSuspendInteractor<Unit, Params>() {
    override suspend fun performAction(params: Params): Result<Unit> {
        return mockConfigurationRepository.finalizeMockedFlow()
    }
}
