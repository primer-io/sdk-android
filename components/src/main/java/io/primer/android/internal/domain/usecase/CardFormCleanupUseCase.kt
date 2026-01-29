package io.primer.android.internal.domain.usecase

import io.primer.android.internal.domain.Cleanable
import io.primer.android.internal.domain.repositories.RawDataManagerRepository

internal class CardFormCleanupUseCase(
    private val rawDataManagerRepository: RawDataManagerRepository,
) : Cleanable {

    override fun cleanup() {
        rawDataManagerRepository.cleanup()
    }
}
