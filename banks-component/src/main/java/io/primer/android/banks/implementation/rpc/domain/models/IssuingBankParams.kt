package io.primer.android.banks.implementation.rpc.domain.models

import io.primer.android.core.domain.Params
import java.util.Locale

internal data class IssuingBankParams(
    val paymentMethodConfigId: String,
    val paymentMethod: String,
    val locale: Locale,
) : Params
