package io.primer.android.domain.rpc.banks.models

import io.primer.android.domain.base.Params
import java.util.Locale

internal data class IssuingBankParams(
    val paymentMethodConfigId: String,
    val paymentMethod: String,
    val locale: Locale,
) : Params
