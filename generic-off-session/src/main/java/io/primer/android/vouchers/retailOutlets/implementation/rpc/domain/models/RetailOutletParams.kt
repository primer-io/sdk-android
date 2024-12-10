package io.primer.android.vouchers.retailOutlets.implementation.rpc.domain.models

import io.primer.android.core.domain.Params

internal data class RetailOutletParams(
    val paymentMethodType: String
) : Params
