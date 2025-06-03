package io.primer.composable.internal.domain.interactor

import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.composable.internal.domain.repositories.RawDataManagerRepository

class GetValidationStateInteractor : DISdkComponent {

    private val rawDataManagerRepository: RawDataManagerRepository by lazy { resolve() }

    fun getValidationState() = rawDataManagerRepository.validationState
}
