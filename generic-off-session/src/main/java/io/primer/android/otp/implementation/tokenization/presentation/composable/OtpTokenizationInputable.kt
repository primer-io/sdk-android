package io.primer.android.otp.implementation.tokenization.presentation.composable

import io.primer.android.PrimerSessionIntent
import io.primer.android.otp.PrimerOtpData
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationInputable

internal data class OtpTokenizationInputable(
    val otpData: PrimerOtpData,
    override val paymentMethodType: String,
    override val primerSessionIntent: PrimerSessionIntent
) : TokenizationInputable
