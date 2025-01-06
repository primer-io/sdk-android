package io.primer.android.vouchers.retailOutlets.implementation.tokenization.presentation.composable

import io.primer.android.PrimerRetailerData
import io.primer.android.PrimerSessionIntent
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationInputable

internal data class RetailOutletsTokenizationInputable(
    val retailOutletData: PrimerRetailerData,
    override val paymentMethodType: String,
    override val primerSessionIntent: PrimerSessionIntent,
) : TokenizationInputable
