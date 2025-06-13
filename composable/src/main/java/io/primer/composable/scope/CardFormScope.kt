package io.primer.composable.scope

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.composable.internal.presentation.screens.card.components.BillingAddressForm
import io.primer.composable.internal.presentation.screens.card.components.CardDetailsForm
import io.primer.composable.internal.presentation.screens.card.components.SubmitButton
import io.primer.composable.internal.presentation.screens.card.components.input.AddressLine1Input
import io.primer.composable.internal.presentation.screens.card.components.input.AddressLine2Input
import io.primer.composable.internal.presentation.screens.card.components.input.CardNumberInput
import io.primer.composable.internal.presentation.screens.card.components.input.CardholderNameInput
import io.primer.composable.internal.presentation.screens.card.components.input.CityInput
import io.primer.composable.internal.presentation.screens.card.components.input.CountryCodeInput
import io.primer.composable.internal.presentation.screens.card.components.input.CvvInput
import io.primer.composable.internal.presentation.screens.card.components.input.ExpiryDateInput
import io.primer.composable.internal.presentation.screens.card.components.input.FirstNameInput
import io.primer.composable.internal.presentation.screens.card.components.input.LastNameInput
import io.primer.composable.internal.presentation.screens.card.components.input.OtpCodeInput
import io.primer.composable.internal.presentation.screens.card.components.input.PhoneNumberInput
import io.primer.composable.internal.presentation.screens.card.components.input.PostalCodeInput
import io.primer.composable.internal.presentation.screens.card.components.input.RetailOutletInput
import io.primer.composable.internal.presentation.screens.card.components.input.StateInput
import kotlinx.coroutines.flow.StateFlow

interface CardFormScope {

    val state: StateFlow<State>

    fun updateCardNumber(cardNumber: String)
    fun updateCvv(cvv: String)
    fun updateExpiryDate(expiryDate: String)
    fun updateCardholderName(cardholderName: String)
    fun updatePostalCode(postalCode: String)
    fun updateCountryCode(countryCode: String)
    fun updateCity(city: String)
    fun updateState(state: String)
    fun updateAddressLine1(addressLine1: String)
    fun updateAddressLine2(addressLine2: String)
    fun updatePhoneNumber(phoneNumber: String)
    fun updateFirstName(firstName: String)
    fun updateLastName(lastName: String)
    fun updateRetailOutlet(retailOutlet: String)
    fun updateOtpCode(otpCode: String)

    fun submit()

    data class State(
        val cardFields: List<PrimerInputElementType> = emptyList(),
        val billingFields: List<PrimerInputElementType> = emptyList(),
        val fieldErrors: List<PrimerInputValidationError> = emptyList(),
        val inputFields: Map<PrimerInputElementType, String> = emptyMap(),
        val isLoading: Boolean = false,
        val isSubmitEnabled: Boolean = false,
    )

    companion object {

        @Composable
        fun CardFormScope.PrimerSubmitButton(
            modifier: Modifier = Modifier,
            text: String = "Submit",
        ) {
            SubmitButton(
                modifier = modifier,
                text = text,
            )
        }

        @Composable
        fun CardFormScope.PrimerCardNumberInput(
            modifier: Modifier = Modifier,
        ) {
            CardNumberInput(modifier)
        }

        @Composable
        fun CardFormScope.PrimerCvvInput(
            modifier: Modifier = Modifier,
        ) {
            CvvInput(modifier)
        }

        @Composable
        fun CardFormScope.PrimerExpiryDateInput(
            modifier: Modifier = Modifier,
        ) {
            ExpiryDateInput(modifier)
        }

        @Composable
        fun CardFormScope.PrimerCardholderNameInput(
            modifier: Modifier = Modifier,
        ) {
            CardholderNameInput(modifier)
        }

        @Composable
        fun CardFormScope.PrimerPostalCodeInput(
            modifier: Modifier = Modifier,
        ) {
            PostalCodeInput(modifier)
        }

        @Composable
        fun CardFormScope.PrimerCountryCodeInput(
            modifier: Modifier = Modifier,
        ) {
            CountryCodeInput(modifier)
        }

        @Composable
        fun CardFormScope.PrimerCityInput(
            modifier: Modifier = Modifier,
        ) {
            CityInput(modifier)
        }

        @Composable
        fun CardFormScope.PrimerStateInput(
            modifier: Modifier = Modifier,
        ) {
            StateInput(modifier)
        }

        @Composable
        fun CardFormScope.PrimerAddressLine1Input(
            modifier: Modifier = Modifier,
        ) {
            AddressLine1Input(modifier)
        }

        @Composable
        fun CardFormScope.PrimerAddressLine2Input(
            modifier: Modifier = Modifier,
        ) {
            AddressLine2Input(modifier)
        }

        @Composable
        fun CardFormScope.PrimerPhoneNumberInput(
            modifier: Modifier = Modifier,
        ) {
            PhoneNumberInput(modifier)
        }

        @Composable
        fun CardFormScope.PrimerFirstNameInput(
            modifier: Modifier = Modifier,
        ) {
            FirstNameInput(modifier)
        }

        @Composable
        fun CardFormScope.PrimerLastNameInput(
            modifier: Modifier = Modifier,
        ) {
            LastNameInput(modifier)
        }

        @Composable
        fun CardFormScope.PrimerRetailOutletInput(
            modifier: Modifier = Modifier,
        ) {
            RetailOutletInput(modifier)
        }

        @Composable
        fun CardFormScope.PrimerOtpCodeInput(
            modifier: Modifier = Modifier,
        ) {
            OtpCodeInput(modifier)
        }

        @Composable
        fun CardFormScope.PrimerCardDetails(
            modifier: Modifier = Modifier,
        ) {
            CardDetailsForm(modifier)
        }

        @Composable
        fun CardFormScope.PrimerBillingAddress(
            modifier: Modifier = Modifier,
        ) {
            BillingAddressForm(modifier)
        }
    }
}
