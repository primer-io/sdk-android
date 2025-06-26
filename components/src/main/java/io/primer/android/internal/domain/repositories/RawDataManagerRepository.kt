package io.primer.android.internal.domain.repositories

import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadataState
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import kotlinx.coroutines.flow.Flow

internal interface RawDataManagerRepository {

    fun init()

    fun getRequiredInputElementTypes(): List<PrimerInputElementType>

    val validationState: Flow<List<PrimerInputValidationError>>

    val metadataState: Flow<PrimerPaymentMethodMetadataState>

    fun setData(data: PrimerCardData)

    fun submit()
}
