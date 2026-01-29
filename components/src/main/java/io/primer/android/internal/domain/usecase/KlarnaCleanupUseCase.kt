package io.primer.android.internal.domain.usecase

import io.primer.android.internal.domain.Cleanable
import io.primer.android.internal.domain.repositories.KlarnaRepository

internal class KlarnaCleanupUseCase(
    private val klarnaRepository: KlarnaRepository,
) : Cleanable {

    override fun cleanup() {
        klarnaRepository.cleanup()
    }
}
