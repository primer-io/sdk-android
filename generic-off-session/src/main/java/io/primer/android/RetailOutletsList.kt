package io.primer.android

import io.primer.android.paymentmethods.PrimerInitializationData
import io.primer.android.vouchers.retailOutlets.implementation.rpc.domain.models.RetailOutlet

data class RetailOutletsList(
    val result: List<RetailOutlet>,
) : PrimerInitializationData
