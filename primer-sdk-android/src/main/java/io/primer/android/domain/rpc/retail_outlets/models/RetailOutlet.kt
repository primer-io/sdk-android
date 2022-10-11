package io.primer.android.domain.rpc.retail_outlets.models

internal data class RetailOutlet(
    val id: String,
    val name: String,
    val disabled: Boolean,
    val iconUrl: String,
)
