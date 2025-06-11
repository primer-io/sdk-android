package io.primer.composable.internal.domain.repositories

import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import kotlinx.coroutines.flow.Flow

internal interface RawDataManagerRepository {

    fun getRequiredInputElementTypes(): List<PrimerInputElementType>

    val validationState: Flow<List<PrimerInputValidationError>>

    fun setData(data: PrimerCardData)

    fun submit()
}
