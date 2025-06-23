package io.primer.composable.internal.presentation.scope

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
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
import io.primer.composable.scope.PrimerSelectCountryScope

internal abstract class DefaultCardFormScope : ViewModel(), PrimerCardFormScope, DISdkComponent {

    override val selectCountry: PrimerSelectCountryScope by lazy { resolve() }

    override var screen: @Composable () -> Unit = {
        DefaultCardFormScreen()
    }

    override var submitButton: @Composable (modifier: Modifier, text: String) -> Unit = { modifier, text ->
        SubmitButton(modifier = modifier, text = text)
    }

    override var cardNumberInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        CardNumberInput(modifier)
    }

    override var cvvInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        CvvInput(modifier)
    }

    override var expiryDateInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        ExpiryDateInput(modifier)
    }

    override var cardholderNameInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        CardholderNameInput(modifier)
    }

    override var postalCodeInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        PostalCodeInput(modifier)
    }

    override var countryCodeInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        CountryCodeInput(modifier)
    }

    override var cityInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        CityInput(modifier)
    }

    override var stateInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        StateInput(modifier)
    }

    override var addressLine1Input: @Composable (modifier: Modifier) -> Unit = { modifier ->
        AddressLine1Input(modifier)
    }

    override var addressLine2Input: @Composable (modifier: Modifier) -> Unit = { modifier ->
        AddressLine2Input(modifier)
    }

    override var phoneNumberInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        PhoneNumberInput(modifier)
    }

    override var firstNameInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        FirstNameInput(modifier)
    }

    override var lastNameInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        LastNameInput(modifier)
    }

    override var retailOutletInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        RetailOutletInput(modifier)
    }

    override var otpCodeInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        OtpCodeInput(modifier)
    }

    override var cardDetails: @Composable (modifier: Modifier) -> Unit = { modifier ->
        CardDetailsForm(modifier)
    }

    override var billingAddress: @Composable (modifier: Modifier) -> Unit = { modifier ->
        BillingAddressForm(modifier)
    }
}
