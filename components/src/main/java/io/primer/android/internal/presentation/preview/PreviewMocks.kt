@file:Suppress("detekt.all")

package io.primer.android.internal.presentation.preview

import io.primer.android.PrimerSessionIntent
import io.primer.android.api.components.card.PrimerCardFormController
import io.primer.android.clientSessionActions.domain.models.PrimerCountry
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.configuration.data.model.CountryCode
import io.primer.android.data.tokenization.models.PaymentInstrumentData
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.internal.domain.models.PrimerComposablePaymentMethod
import io.primer.android.internal.presentation.utils.VaultItemProperties
import io.primer.android.scope.PrimerCountrySelectionScope
import kotlinx.coroutines.flow.MutableStateFlow

internal val mockPaymentMethods: List<PrimerComposablePaymentMethod>
    get() = PreviewMocks.paymentMethods

internal object PreviewMocks {

    // ==================== Mock Data ====================

    val country = PrimerCountry(
        name = "United States",
        code = CountryCode.US,
    )

    val countries = listOf(
        PrimerCountry("United States", CountryCode.US),
        PrimerCountry("United Kingdom", CountryCode.GB),
        PrimerCountry("Germany", CountryCode.DE),
        PrimerCountry("France", CountryCode.FR),
    )

    val vaultedPaymentMethod = PrimerVaultedPaymentMethod(
        id = "vault-id-123",
        analyticsId = "analytics-123",
        paymentInstrumentType = "PAYMENT_CARD",
        paymentMethodType = "PAYMENT_CARD",
        paymentInstrumentData = PaymentInstrumentData(
            network = "Visa",
            cardholderName = "John Doe",
            first6Digits = 424242,
            last4Digits = 4242,
            expirationMonth = 12,
            expirationYear = 2025,
        ),
    )

    val vaultedPaymentMethods = listOf(vaultedPaymentMethod)

    val vaultItemProperties = VaultItemProperties(
        paymentMethod = vaultedPaymentMethod,
        isSelected = false,
    )

    val paymentMethods = listOf(
        PrimerComposablePaymentMethod(
            paymentMethodType = "PAYMENT_CARD",
            paymentMethodName = "Card",
            supportedPrimerSessionIntents = listOf(PrimerSessionIntent.CHECKOUT),
            paymentMethodManagerCategories = listOf(PrimerPaymentMethodManagerCategory.RAW_DATA),
        ),
        PrimerComposablePaymentMethod(
            paymentMethodType = "GOOGLE_PAY",
            paymentMethodName = "Google Pay",
            supportedPrimerSessionIntents = listOf(PrimerSessionIntent.CHECKOUT),
            paymentMethodManagerCategories = listOf(PrimerPaymentMethodManagerCategory.NATIVE_UI),
        ),
        PrimerComposablePaymentMethod(
            paymentMethodType = "KLARNA",
            paymentMethodName = "Klarna",
            supportedPrimerSessionIntents = listOf(PrimerSessionIntent.CHECKOUT),
            paymentMethodManagerCategories = listOf(PrimerPaymentMethodManagerCategory.NATIVE_UI),
        ),
    )

    // ==================== Default States ====================

    val cardFormState = PrimerCardFormController.State(
        cardFields = listOf(
            PrimerInputElementType.CARD_NUMBER,
            PrimerInputElementType.EXPIRY_DATE,
            PrimerInputElementType.CVV,
            PrimerInputElementType.CARDHOLDER_NAME,
        ),
        billingFields = listOf(
            PrimerInputElementType.COUNTRY_CODE,
            PrimerInputElementType.FIRST_NAME,
            PrimerInputElementType.LAST_NAME,
            PrimerInputElementType.ADDRESS_LINE_1,
            PrimerInputElementType.ADDRESS_LINE_2,
            PrimerInputElementType.POSTAL_CODE,
            PrimerInputElementType.CITY,
            PrimerInputElementType.STATE,
        ),
        data = mapOf(
            PrimerInputElementType.CARD_NUMBER to "4242424242424242",
            PrimerInputElementType.EXPIRY_DATE to "12/25",
            PrimerInputElementType.CVV to "123",
            PrimerInputElementType.CARDHOLDER_NAME to "John Doe",
            PrimerInputElementType.COUNTRY_CODE to "US",
            PrimerInputElementType.FIRST_NAME to "John",
            PrimerInputElementType.LAST_NAME to "Doe",
            PrimerInputElementType.ADDRESS_LINE_1 to "123 Main St",
            PrimerInputElementType.ADDRESS_LINE_2 to "Apt 4B",
            PrimerInputElementType.POSTAL_CODE to "10001",
            PrimerInputElementType.CITY to "New York",
            PrimerInputElementType.STATE to "NY",
        ),
        isFormValid = true,
        isFormEnabled = true,
        selectedCountry = country,
    )

    val countrySelectionState = PrimerCountrySelectionScope.State(
        countries = countries,
        filteredCountries = countries,
        searchQuery = "",
    )

    // ==================== Mock Scopes ====================

    val countrySelectionScope: PrimerCountrySelectionScope =
        createCountrySelectionScope(countrySelectionState)

    // ==================== Factory Functions ====================

    fun createCountrySelectionScope(state: PrimerCountrySelectionScope.State) =
        object : PrimerCountrySelectionScope {
            override val state = MutableStateFlow(state)
            override fun onCountrySelected(countryCode: String, countryName: String) {}
            override fun onSearch(query: String) {}
        }
}
