package io.primer.composable.internal.domain.interactor

import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.configuration.di.ConfigurationCoreContainer.Companion.CONFIGURATION_INTERACTOR_DI_KEY
import io.primer.android.configuration.domain.CachePolicy
import io.primer.android.configuration.domain.ConfigurationInteractor
import io.primer.android.configuration.domain.model.CheckoutModule
import io.primer.android.configuration.domain.model.ConfigurationParams
import io.primer.android.configuration.domain.model.findFirstInstance
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve

class GetCardFieldsInteractor: DISdkComponent {

    private val configurationInteractor: ConfigurationInteractor by lazy { resolve(CONFIGURATION_INTERACTOR_DI_KEY) }

    suspend fun getCardFields(): List<PrimerInputElementType> {
        val result = configurationInteractor(ConfigurationParams(CachePolicy.ForceCache))
        val cardInfoModule = result.getOrThrow().checkoutModules.findFirstInstance<CheckoutModule.CardInformation>()

        if (cardInfoModule == null) {
            return emptyList()
        }
        
        val defaultCardFields = listOf(
            PrimerInputElementType.CARD_NUMBER,
            PrimerInputElementType.EXPIRY_DATE,
            PrimerInputElementType.CVV,
            PrimerInputElementType.CARDHOLDER_NAME
        )
        
        return filterAvailableFields(cardInfoModule.options, defaultCardFields)
    }

    suspend fun getBillingFields(): List<PrimerInputElementType> {
        val result = configurationInteractor(ConfigurationParams(CachePolicy.ForceCache))
        val billingModule = result.getOrThrow().checkoutModules.findFirstInstance<CheckoutModule.BillingAddress>()

        if (billingModule == null) {
            return emptyList()
        }
        
        val defaultBillingFields = listOf(
            PrimerInputElementType.COUNTRY_CODE,
            PrimerInputElementType.FIRST_NAME,
            PrimerInputElementType.LAST_NAME,
            PrimerInputElementType.ADDRESS_LINE_1,
            PrimerInputElementType.ADDRESS_LINE_2,
            PrimerInputElementType.POSTAL_CODE,
            PrimerInputElementType.CITY,
            PrimerInputElementType.STATE
        )
        
        return filterAvailableFields(billingModule.options, defaultBillingFields)
    }

    private fun filterAvailableFields(
        fieldOptions: Map<String, Boolean>?, 
        defaultFields: List<PrimerInputElementType>
    ): List<PrimerInputElementType> {
        if (fieldOptions == null || fieldOptions[PrimerInputElementType.ALL.field] == true) {
            return defaultFields
        }
        
        return defaultFields.filter { fieldOptions[it.field] == true }
    }
}
