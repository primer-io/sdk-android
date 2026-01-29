package io.primer.android.internal.data.repositories

import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadataState
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManager
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManagerInterface
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManagerListener
import io.primer.android.internal.domain.repositories.RawDataManagerRepository
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

class CardRawDataManagerRepository(
    private val cardManager: PrimerHeadlessUniversalCheckoutRawDataManagerInterface =
        PrimerHeadlessUniversalCheckoutRawDataManager.newInstance(PaymentMethodType.PAYMENT_CARD.name),
) : RawDataManagerRepository {

    private sealed class RawDataManagerEvent {
        data class ValidationChanged(
            val isValid: Boolean,
            val errors: List<PrimerInputValidationError>,
        ) : RawDataManagerEvent()
        data class MetadataChanged(val state: PrimerPaymentMethodMetadataState) : RawDataManagerEvent()
    }

    private val events = callbackFlow {
        val listener = object : PrimerHeadlessUniversalCheckoutRawDataManagerListener {
            override fun onValidationChanged(
                isValid: Boolean,
                errors: List<PrimerInputValidationError>,
            ) {
                trySend(RawDataManagerEvent.ValidationChanged(isValid, errors))
            }

            override fun onMetadataStateChanged(metadataState: PrimerPaymentMethodMetadataState) {
                trySend(RawDataManagerEvent.MetadataChanged(metadataState))
            }
        }

        cardManager.setListener(listener)

        awaitClose {
            cardManager.cleanup()
        }
    }.shareIn(
        scope = CoroutineScope(Dispatchers.Main),
        started = SharingStarted.Lazily,
        replay = 1,
    )

    override fun getRequiredInputElementTypes(): List<PrimerInputElementType> {
        return cardManager.getRequiredInputElementTypes()
    }

    override val validationState: Flow<List<PrimerInputValidationError>> = events
        .filterIsInstance<RawDataManagerEvent.ValidationChanged>()
        .map { it.errors }

    override val metadataState: Flow<PrimerPaymentMethodMetadataState> = events
        .filterIsInstance<RawDataManagerEvent.MetadataChanged>()
        .map { it.state }

    override fun setData(data: PrimerCardData) = cardManager.setRawData(data)

    override fun submit() = cardManager.submit()

    override fun cleanup() = cardManager.cleanup()
}
