package io.primer.android.internal.domain.usecase

import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.internal.domain.repositories.RawDataManagerRepository

class InitCardManagerUseCase : DISdkComponent {

    private val rawDataManagerRepository: RawDataManagerRepository by lazy { resolve() }

    operator fun invoke() {
        rawDataManagerRepository.init()
    }
}
