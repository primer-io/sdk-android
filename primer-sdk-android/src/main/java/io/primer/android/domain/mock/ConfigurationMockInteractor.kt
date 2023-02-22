package io.primer.android.domain.mock

import io.primer.android.domain.base.BaseInteractor
import io.primer.android.domain.base.None
import io.primer.android.domain.mock.repository.ConfigurationMockRepository

internal class ConfigurationMockInteractor(
    private val configurationMockRepository: ConfigurationMockRepository
) : BaseInteractor<Boolean, None>() {
    override fun execute(params: None) = configurationMockRepository.isMockedFlow()
}
