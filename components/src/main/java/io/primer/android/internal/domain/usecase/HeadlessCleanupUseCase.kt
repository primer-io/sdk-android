package io.primer.android.internal.domain.usecase

import io.primer.android.internal.domain.repositories.HeadlessRepository

internal class HeadlessCleanupUseCase(
    private val headlessRepository: HeadlessRepository,
) {

    operator fun invoke() = headlessRepository.cleanup()
}
