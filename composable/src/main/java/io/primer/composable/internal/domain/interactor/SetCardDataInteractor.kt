package io.primer.composable.internal.domain.interactor

import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.composable.internal.domain.repositories.RawDataManagerRepository

class SetCardDataInteractor : DISdkComponent {

    private val rawDataManagerRepository: RawDataManagerRepository by lazy { resolve() }

    private var data: PrimerCardData? = null

    operator fun invoke(content: Pair<PrimerInputElementType, String>) {
        // Initialize data if null
        if (data == null) {
            data = PrimerCardData(
                cardNumber = "",
                expiryDate = "",
                cvv = "",
                cardHolderName = null,
                cardNetwork = null,
            )
        }

        // Update the specific field based on the input type
        data = when (content.first) {
            PrimerInputElementType.CARD_NUMBER -> data?.copy(cardNumber = content.second)
            PrimerInputElementType.EXPIRY_DATE -> data?.copy(expiryDate = content.second)
            PrimerInputElementType.CVV -> data?.copy(cvv = content.second)
            PrimerInputElementType.CARDHOLDER_NAME -> data?.copy(cardHolderName = content.second.takeIf { it.isNotEmpty() })
            else -> data // For other types, keep the current data
        }

        // Update the repository with the new data
        data?.let { rawDataManagerRepository.setData(it) }
    }
}
