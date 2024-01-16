package io.primer.android.domain.rpc.banks.models

data class IssuingBank(
    val id: String,
    val name: String,
    val disabled: Boolean,
    val iconUrl: String
)
