package io.primer.android.configuration.mock.presentation

import io.primer.android.configuration.mock.domain.MockConfigurationInteractor
import io.primer.android.core.domain.None

class MockConfigurationDelegate internal constructor(
    private val interactor: MockConfigurationInteractor,
) {
    fun isMockedFlow() = interactor.execute(None)
}
