package io.primer.composable.internal.domain.interactor

import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.composable.internal.domain.repositories.RawDataManagerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SetCardDataInteractor : DISdkComponent {

    private val rawDataManagerRepository: RawDataManagerRepository by lazy { resolve() }
    private val trackDirtyFieldsInteractor: TrackDirtyFieldsInteractor by lazy { resolve() }

    private val _inputData = MutableStateFlow<MutableMap<PrimerInputElementType, String>>(mutableMapOf())
    val inputData: Flow<Map<PrimerInputElementType, String>> = _inputData.asStateFlow()

    fun updateInput(input: String, type: PrimerInputElementType) {
        _inputData.update { it.toMutableMap().apply { this[type] = input } }
        trackDirtyFieldsInteractor.markFieldAsDirty(type)
        rawDataManagerRepository.setData(buildPrimerCardData())
    }

    private fun buildPrimerCardData() = PrimerCardData(
        cardNumber = _inputData.value[PrimerInputElementType.CARD_NUMBER] ?: "",
        expiryDate = formatExpiryDate(_inputData.value[PrimerInputElementType.EXPIRY_DATE] ?: ""),
        cvv = _inputData.value[PrimerInputElementType.CVV] ?: "",
        cardHolderName = _inputData.value[PrimerInputElementType.CARDHOLDER_NAME]?.takeIf { it.isNotEmpty() },
        cardNetwork = null,
    )

    private fun formatExpiryDate(input: String): String {
        if (input.isEmpty()) return input

        return when {
            input.length <= MONTH_LENGTH -> input
            input.length == FULL_EXPIRY_LENGTH -> "${input.take(MONTH_LENGTH)}/${input.drop(MONTH_LENGTH)}"
            else -> input // Return as-is for incomplete input
        }
    }

    companion object {
        private const val MONTH_LENGTH = 2
        private const val FULL_EXPIRY_LENGTH = 6
    }
}
