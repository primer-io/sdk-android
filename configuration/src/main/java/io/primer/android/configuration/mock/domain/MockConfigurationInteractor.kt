package io.primer.android.configuration.mock.domain

import io.primer.android.configuration.mock.domain.repository.MockConfigurationRepository
import io.primer.android.core.domain.BaseInteractor
import io.primer.android.core.domain.None

internal class MockConfigurationInteractor(
    private val mockConfigurationRepository: MockConfigurationRepository,
) : BaseInteractor<Boolean, None>() {
    override fun execute(params: None) = mockConfigurationRepository.isMockedFlow()
}
