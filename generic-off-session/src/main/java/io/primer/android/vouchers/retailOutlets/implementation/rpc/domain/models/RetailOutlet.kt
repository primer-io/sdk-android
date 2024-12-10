package io.primer.android.vouchers.retailOutlets.implementation.rpc.domain.models

data class RetailOutlet(
    val id: String,
    val name: String,
    val disabled: Boolean,
    val iconUrl: String
)
