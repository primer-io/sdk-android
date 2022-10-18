package io.primer.android.domain.rpc.retailOutlets.models

data class RetailOutlet(
    val id: String,
    val name: String,
    val disabled: Boolean,
    val iconUrl: String,
)
