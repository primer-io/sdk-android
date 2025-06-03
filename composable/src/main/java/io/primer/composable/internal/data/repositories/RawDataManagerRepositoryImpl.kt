package io.primer.composable.internal.data.repositories

import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManagerInterface
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManagerListener
import io.primer.android.core.di.DISdkComponent
import io.primer.android.paymentmethods.PrimerRawData
import io.primer.composable.internal.domain.repositories.RawDataManagerRepository

class RawDataManagerRepositoryImpl(
    private val cardManager: PrimerHeadlessUniversalCheckoutRawDataManagerInterface
) : RawDataManagerRepository, DISdkComponent {

    override fun getRequiredInputElementTypes(): List<PrimerInputElementType> = cardManager.getRequiredInputElementTypes()

    override fun setListener(listener: PrimerHeadlessUniversalCheckoutRawDataManagerListener) {
        cardManager.setListener(listener)
    }

    override fun setRawData(rawData: PrimerRawData) {
        cardManager.setRawData(rawData)
    }

}
