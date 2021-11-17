package io.primer.android.domain.rpc.banks.models

import io.primer.android.domain.base.Params
import io.primer.android.model.dto.PaymentMethodType
import java.util.Locale

internal data class IssuingBankParams(
    val paymentMethodConfigId: String,
    val paymentMethod: PaymentMethodType,
    val locale: Locale,
) : Params
