package io.primer.composable.internal.domain.interactor

import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.uicore.domain.helper.toSyncValidationError
import io.primer.android.uicore.model.SyncValidationError
import io.primer.composable.internal.domain.repositories.RawDataManagerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetValidationStateInteractor : DISdkComponent {

    private val rawDataManagerRepository: RawDataManagerRepository by lazy { resolve() }
    private val trackDirtyFieldsInteractor: TrackDirtyFieldsInteractor by lazy { resolve() }
    private val setCardDataInteractor: SetCardDataInteractor by lazy { resolve() }

    val validationState: Flow<List<SyncValidationError>> =
        combine(
            rawDataManagerRepository.validationState,
            trackDirtyFieldsInteractor.dirtyFields,
            setCardDataInteractor.inputData,
        ) { errors, dirtyFields, cardData ->
            val primerCardData = PrimerCardData(
                cardNumber = cardData[PrimerInputElementType.CARD_NUMBER] ?: "",
                expiryDate = cardData[PrimerInputElementType.EXPIRY_DATE] ?: "",
                cvv = cardData[PrimerInputElementType.CVV] ?: "",
                cardHolderName = cardData[PrimerInputElementType.CARDHOLDER_NAME]?.takeIf { it.isNotEmpty() },
                cardNetwork = CardNetwork.Type.OTHER // Default - could be enhanced to detect actual network
            )

            errors.map { it.toSyncValidationError(primerCardData) }.filter { error ->
                error.inputElementType in dirtyFields
            }
        }

    val isSubmitAllowed: Flow<Boolean> =
        combine(
            rawDataManagerRepository.validationState,
            trackDirtyFieldsInteractor.dirtyFields,
        ) { errors, dirtyFields ->
            val requiredFields = rawDataManagerRepository.getRequiredInputElementTypes()
            val allRequiredFieldsTouched = requiredFields.all { it in dirtyFields }

            allRequiredFieldsTouched && errors.isEmpty()
        }
}
