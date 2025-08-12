package io.primer.android.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.internal.presentation.screens.card.CardFormViewModel
import io.primer.android.internal.presentation.screens.card.CardFormViewModelFactory
import io.primer.android.internal.presentation.screens.card.DefaultCardFormScreen
import io.primer.android.internal.presentation.screens.card.components.BillingAddressForm
import io.primer.android.internal.presentation.screens.card.components.CardDetailsForm
import io.primer.android.internal.presentation.screens.card.components.CardNetwork
import io.primer.android.internal.presentation.screens.card.components.SubmitButton
import io.primer.android.internal.presentation.screens.card.components.input.AddressLine1Input
import io.primer.android.internal.presentation.screens.card.components.input.AddressLine2Input
import io.primer.android.internal.presentation.screens.card.components.input.CardNumberInput
import io.primer.android.internal.presentation.screens.card.components.input.CardholderNameInput
import io.primer.android.internal.presentation.screens.card.components.input.CityInput
import io.primer.android.internal.presentation.screens.card.components.input.CountryCodeInput
import io.primer.android.internal.presentation.screens.card.components.input.CvvInput
import io.primer.android.internal.presentation.screens.card.components.input.ExpiryDateInput
import io.primer.android.internal.presentation.screens.card.components.input.FirstNameInput
import io.primer.android.internal.presentation.screens.card.components.input.LastNameInput
import io.primer.android.internal.presentation.screens.card.components.input.PhoneNumberInput
import io.primer.android.internal.presentation.screens.card.components.input.PostalCodeInput
import io.primer.android.internal.presentation.screens.card.components.input.StateInput
import io.primer.android.scope.PrimerCardFormScope

class PrimerCardFormComponents : DISdkComponent {

    val selectCountry: PrimerSelectCountryComponents
        get() = resolve()

    @Composable
    fun Screen() {
        screen(viewModel<CardFormViewModel>(factory = resolve<CardFormViewModelFactory>()))
    }

    /**
     * Composable function for the entire card form screen layout.
     */
    var screen: @Composable PrimerCardFormScope.() -> Unit = {
        DefaultCardFormScreen()
    }

    /**
     * Composable function for the submit button with customizable styling and text.
     *
     * @param modifier Modifier for styling the button
     * @param text Text to display on the button
     */
    var submitButton: @Composable PrimerCardFormScope.(modifier: Modifier, text: String) -> Unit = { modifier, text ->
        SubmitButton(modifier = modifier, text = text)
    }

    /**
     * Composable function for the card number input field with validation.
     *
     * @param modifier Modifier for styling the input field
     */
    var cardNumberInput: @Composable PrimerCardFormScope.(modifier: Modifier) -> Unit = {
        CardNumberInput(it)
    }

    /**
     * Composable function for the CVV input field with validation.
     *
     * @param modifier Modifier for styling the input field
     */
    var cvvInput: @Composable PrimerCardFormScope.(modifier: Modifier) -> Unit = {
        CvvInput(it)
    }

    /**
     * Composable function for the expiry date input field with validation.
     *
     * @param modifier Modifier for styling the input field
     */
    var expiryDateInput: @Composable PrimerCardFormScope.(modifier: Modifier) -> Unit = {
        ExpiryDateInput(it)
    }

    /**
     * Composable function for the cardholder name input field with validation.
     *
     * @param modifier Modifier for styling the input field
     */
    var cardholderNameInput: @Composable PrimerCardFormScope.(modifier: Modifier) -> Unit = {
        CardholderNameInput(it)
    }

    /**
     * Composable function for the postal code input field with validation.
     *
     * @param modifier Modifier for styling the input field
     */
    var postalCodeInput: @Composable PrimerCardFormScope.(modifier: Modifier) -> Unit = {
        PostalCodeInput(it)
    }

    /**
     * Composable function for the country code input field with validation.
     *
     * @param modifier Modifier for styling the input field
     */
    var countryCodeInput: @Composable PrimerCardFormScope.(modifier: Modifier) -> Unit = {
        CountryCodeInput(it)
    }

    /**
     * Composable function for the city input field with validation.
     *
     * @param modifier Modifier for styling the input field
     */
    var cityInput: @Composable PrimerCardFormScope.(modifier: Modifier) -> Unit = {
        CityInput(it)
    }

    /**
     * Composable function for the state/province input field with validation.
     *
     * @param modifier Modifier for styling the input field
     */
    var stateInput: @Composable PrimerCardFormScope.(modifier: Modifier) -> Unit = {
        StateInput(it)
    }

    /**
     * Composable function for the first address line input field with validation.
     *
     * @param modifier Modifier for styling the input field
     */
    var addressLine1Input: @Composable PrimerCardFormScope.(modifier: Modifier) -> Unit = {
        AddressLine1Input(it)
    }

    /**
     * Composable function for the second address line input field with validation.
     *
     * @param modifier Modifier for styling the input field
     */
    var addressLine2Input: @Composable PrimerCardFormScope.(modifier: Modifier) -> Unit = {
        AddressLine2Input(it)
    }

    /**
     * Composable function for the phone number input field with validation.
     *
     * @param modifier Modifier for styling the input field
     */
    var phoneNumberInput: @Composable PrimerCardFormScope.(modifier: Modifier) -> Unit = {
        PhoneNumberInput(it)
    }

    /**
     * Composable function for the first name input field with validation.
     *
     * @param modifier Modifier for styling the input field
     */
    var firstNameInput: @Composable PrimerCardFormScope.(modifier: Modifier) -> Unit = {
        FirstNameInput(it)
    }

    /**
     * Composable function for the last name input field with validation.
     *
     * @param modifier Modifier for styling the input field
     */
    var lastNameInput: @Composable PrimerCardFormScope.(modifier: Modifier) -> Unit = {
        LastNameInput(it)
    }

    /**
     * Composable function for displaying grouped card details section.
     *
     * @param modifier Modifier for styling the section
     */
    var cardDetails: @Composable PrimerCardFormScope.(modifier: Modifier) -> Unit = {
        CardDetailsForm(it)
    }

    /**
     * Composable function for displaying grouped billing address section.
     *
     * @param modifier Modifier for styling the section
     */
    var billingAddress: @Composable PrimerCardFormScope.(modifier: Modifier) -> Unit = {
        BillingAddressForm(it)
    }

    /**
     * Composable function for displaying card network selection interface.
     *
     * @param modifier Modifier for styling the network selector
     */
    var cardNetwork: @Composable PrimerCardFormScope.(modifier: Modifier) -> Unit = {
        CardNetwork()
    }
}
