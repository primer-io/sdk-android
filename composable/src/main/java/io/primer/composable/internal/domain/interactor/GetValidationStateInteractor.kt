package io.primer.composable.internal.domain.interactor

import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.composable.internal.domain.repositories.RawDataManagerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetValidationStateInteractor : DISdkComponent {

    private val rawDataManagerRepository: RawDataManagerRepository by lazy { resolve() }
    private val trackDirtyFieldsInteractor: TrackDirtyFieldsInteractor by lazy { resolve() }

    val validationState: Flow<List<PrimerInputValidationError>> =
        combine(
            rawDataManagerRepository.validationState,
            trackDirtyFieldsInteractor.dirtyFields,
        ) { errors, dirtyFields ->
            errors.filter { error ->
                error.inputElementType in dirtyFields
            }
        }

    val isSubmitAllowed: Flow<Boolean> =
        combine(
            rawDataManagerRepository.validationState,
            trackDirtyFieldsInteractor.dirtyFields,
        ) { errors, dirtyFields ->
            val requiredFields = rawDataManagerRepository.getRequiredInputElementTypes()
            val allRequiredFieldsTouched = requiredFields.all { it in dirtyFields }

            allRequiredFieldsTouched && errors.isEmpty()
        }
}
