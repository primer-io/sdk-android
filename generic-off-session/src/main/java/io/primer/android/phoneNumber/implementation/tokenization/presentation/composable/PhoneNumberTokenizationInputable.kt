package io.primer.android.phoneNumber.implementation.tokenization.presentation.composable

import io.primer.android.PrimerSessionIntent
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationInputable
import io.primer.android.phoneNumber.PrimerPhoneNumberData

internal data class PhoneNumberTokenizationInputable(
    val phoneNumberData: PrimerPhoneNumberData,
    override val paymentMethodType: String,
    override val primerSessionIntent: PrimerSessionIntent,
) : TokenizationInputable
