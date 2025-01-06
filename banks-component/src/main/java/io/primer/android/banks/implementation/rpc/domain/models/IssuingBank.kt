package io.primer.android.banks.implementation.rpc.domain.models

data class IssuingBank(
    val id: String,
    val name: String,
    val disabled: Boolean,
    val iconUrl: String,
)
