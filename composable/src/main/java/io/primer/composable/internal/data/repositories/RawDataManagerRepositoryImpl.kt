package io.primer.composable.internal.data.repositories

import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManagerInterface
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManagerListener
import io.primer.android.core.di.DISdkComponent
import io.primer.composable.internal.domain.repositories.RawDataManagerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn

class RawDataManagerRepositoryImpl(
    private val cardManager: PrimerHeadlessUniversalCheckoutRawDataManagerInterface,
) : RawDataManagerRepository, DISdkComponent {

    override fun getRequiredInputElementTypes(): List<PrimerInputElementType> =
        cardManager.getRequiredInputElementTypes()

    override val validationState: Flow<List<PrimerInputValidationError>> = callbackFlow {
        cardManager.setListener(object : PrimerHeadlessUniversalCheckoutRawDataManagerListener {
            override fun onValidationChanged(
                isValid: Boolean,
                errors: List<PrimerInputValidationError>,
            ) {
                trySend(errors)
            }
        })

        awaitClose {
            cardManager.cleanup()
        }
    }.shareIn(
        scope = CoroutineScope(Dispatchers.Main),
        started = SharingStarted.Lazily,
        replay = 1,
    )

    override fun setData(data: PrimerCardData) = cardManager.setRawData(data)

    override fun submit() = cardManager.submit()
}
