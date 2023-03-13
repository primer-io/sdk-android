package io.primer.android.domain.mock

import io.primer.android.domain.base.BaseInteractor
import io.primer.android.domain.base.None
import io.primer.android.domain.mock.repository.MockConfigurationRepository

internal class MockConfigurationInteractor(
    private val mockConfigurationRepository: MockConfigurationRepository
) : BaseInteractor<Boolean, None>() {
    override fun execute(params: None) = mockConfigurationRepository.isMockedFlow()
}
