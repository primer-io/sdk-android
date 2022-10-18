package io.primer.android.data.payments.configure.retailOutlets

import io.primer.android.data.payments.configure.PrimerInitializationData
import io.primer.android.domain.rpc.retailOutlets.models.RetailOutlet

data class RetailOutletsList(
    val result: List<RetailOutlet>
) : PrimerInitializationData
