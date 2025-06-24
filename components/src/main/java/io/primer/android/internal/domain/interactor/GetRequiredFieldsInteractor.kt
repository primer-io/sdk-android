package io.primer.android.internal.domain.interactor

import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.internal.domain.repositories.RawDataManagerRepository

class GetRequiredFieldsInteractor : DISdkComponent {

    private val rawDataManagerRepository: RawDataManagerRepository by lazy { resolve() }

    fun getCardFields(): List<PrimerInputElementType> =
        rawDataManagerRepository.getRequiredInputElementTypes().filter { field ->
            field == PrimerInputElementType.CARD_NUMBER ||
                field == PrimerInputElementType.CVV ||
                field == PrimerInputElementType.EXPIRY_DATE ||
                field == PrimerInputElementType.CARDHOLDER_NAME
        }

    fun getBillingFields(): List<PrimerInputElementType> =
        rawDataManagerRepository.getRequiredInputElementTypes().filter { field ->
            field == PrimerInputElementType.POSTAL_CODE ||
                field == PrimerInputElementType.COUNTRY_CODE ||
                field == PrimerInputElementType.CITY ||
                field == PrimerInputElementType.STATE ||
                field == PrimerInputElementType.ADDRESS_LINE_1 ||
                field == PrimerInputElementType.ADDRESS_LINE_2 ||
                field == PrimerInputElementType.FIRST_NAME ||
                field == PrimerInputElementType.LAST_NAME
        }
}
