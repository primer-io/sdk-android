package io.primer.android.domain.rpc.retail_outlets.models

import io.primer.android.domain.base.Params
import java.util.Locale

internal data class RetailOutletParams(
    val paymentMethodConfigId: String,
    val selectedRetailOutlet: String,
    val locale: Locale,
) : Params
