package io.primer.composable.internal.domain.repositories

import io.primer.android.components.domain.inputs.models.PrimerInputElementType

internal interface RawDataManagerRepository {

    fun getRequiredInputElementTypes(): List<PrimerInputElementType>

}
