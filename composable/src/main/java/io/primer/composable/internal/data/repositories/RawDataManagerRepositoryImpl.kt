package io.primer.composable.internal.data.repositories

import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManagerInterface
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManagerListener
import io.primer.android.core.di.DISdkComponent
import io.primer.android.paymentmethods.PrimerRawData
import io.primer.composable.internal.domain.repositories.RawDataManagerRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class RawDataManagerRepositoryImpl(
    private val cardManager: PrimerHeadlessUniversalCheckoutRawDataManagerInterface
) : RawDataManagerRepository, DISdkComponent {

    override fun getRequiredInputElementTypes(): List<PrimerInputElementType> =
        cardManager.getRequiredInputElementTypes()

    override val validationState: Flow<List<PrimerInputValidationError>> = callbackFlow {
        cardManager.setListener(object : PrimerHeadlessUniversalCheckoutRawDataManagerListener {
            override fun onValidationChanged(
                isValid: Boolean,
                errors: List<PrimerInputValidationError>
            ) {
                trySend(errors)
            }
        })

        awaitClose {
            cardManager.cleanup()
        }
    }

    override fun setRawData(rawData: PrimerRawData) {
        cardManager.setRawData(rawData)
    }

}
