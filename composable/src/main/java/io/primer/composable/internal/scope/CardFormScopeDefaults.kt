package io.primer.composable.internal.scope

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.composable.internal.presentation.screens.card.DefaultCardFormScreen
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
import io.primer.composable.scope.PrimerCardFormScope

internal abstract class CardFormScopeDefaults : PrimerCardFormScope {
    
    // Default composable implementations
    override var PrimerCardFormScreen: @Composable () -> Unit = {
        DefaultCardFormScreen()
    }
    
    override var PrimerSubmitButton: @Composable (modifier: Modifier, text: String) -> Unit = { modifier, text ->
        SubmitButton(modifier = modifier, text = text)
    }
    
    override var PrimerCardNumberInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        CardNumberInput(modifier)
    }
    
    override var PrimerCvvInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        CvvInput(modifier)
    }
    
    override var PrimerExpiryDateInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        ExpiryDateInput(modifier)
    }
    
    override var PrimerCardholderNameInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        CardholderNameInput(modifier)
    }
    
    override var PrimerPostalCodeInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        PostalCodeInput(modifier)
    }
    
    override var PrimerCountryCodeInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        CountryCodeInput(modifier)
    }
    
    override var PrimerCityInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        CityInput(modifier)
    }
    
    override var PrimerStateInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        StateInput(modifier)
    }
    
    override var PrimerAddressLine1Input: @Composable (modifier: Modifier) -> Unit = { modifier ->
        AddressLine1Input(modifier)
    }
    
    override var PrimerAddressLine2Input: @Composable (modifier: Modifier) -> Unit = { modifier ->
        AddressLine2Input(modifier)
    }
    
    override var PrimerPhoneNumberInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        PhoneNumberInput(modifier)
    }
    
    override var PrimerFirstNameInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        FirstNameInput(modifier)
    }
    
    override var PrimerLastNameInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        LastNameInput(modifier)
    }
    
    override var PrimerRetailOutletInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        RetailOutletInput(modifier)
    }
    
    override var PrimerOtpCodeInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        OtpCodeInput(modifier)
    }
    
    override var PrimerCardDetails: @Composable (modifier: Modifier) -> Unit = { modifier ->
        CardDetailsForm(modifier)
    }
    
    override var PrimerBillingAddress: @Composable (modifier: Modifier) -> Unit = { modifier ->
        BillingAddressForm(modifier)
    }
}