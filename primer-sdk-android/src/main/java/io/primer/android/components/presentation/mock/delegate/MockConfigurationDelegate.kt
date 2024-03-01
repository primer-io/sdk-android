package io.primer.android.components.presentation.mock.delegate

import io.primer.android.domain.base.None
import io.primer.android.domain.mock.MockConfigurationInteractor

internal class MockConfigurationDelegate(
    private val interactor: MockConfigurationInteractor
) {
    fun isMockedFlow() = interactor.execute(None())
}
