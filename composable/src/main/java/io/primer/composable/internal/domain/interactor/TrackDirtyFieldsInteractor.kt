package io.primer.composable.internal.domain.interactor

import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.core.di.DISdkComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TrackDirtyFieldsInteractor : DISdkComponent {

    private val _dirtyFields = MutableStateFlow<Set<PrimerInputElementType>>(emptySet())
    val dirtyFields: Flow<Set<PrimerInputElementType>> = _dirtyFields.asStateFlow()

    fun markFieldAsDirty(inputElementType: PrimerInputElementType) {
        _dirtyFields.update { it + inputElementType }
    }
}
