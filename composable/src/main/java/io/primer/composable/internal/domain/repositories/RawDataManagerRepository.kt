package io.primer.composable.internal.domain.repositories

import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.paymentmethods.PrimerRawData
import kotlinx.coroutines.flow.Flow

internal interface RawDataManagerRepository {

    fun getRequiredInputElementTypes(): List<PrimerInputElementType>

    val validationState: Flow<List<PrimerInputValidationError>>

    fun setRawData(rawData: PrimerRawData)

}
