package io.primer.composable.internal.domain.repositories

import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManagerListener
import io.primer.android.paymentmethods.PrimerRawData

internal interface RawDataManagerRepository {

    fun getRequiredInputElementTypes(): List<PrimerInputElementType>

    fun setListener(listener: PrimerHeadlessUniversalCheckoutRawDataManagerListener)

    fun setRawData(rawData: PrimerRawData)

}
