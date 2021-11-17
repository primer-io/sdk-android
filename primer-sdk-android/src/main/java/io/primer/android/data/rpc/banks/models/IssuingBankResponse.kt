package io.primer.android.data.rpc.banks.models

import kotlinx.serialization.Serializable

@Serializable
internal data class IssuingBankResultResponse(
    val result: List<IssuingBankResponse>,
)

@Serializable
internal data class IssuingBankResponse(
    val id: String,
    val name: String,
    val disabled: Boolean,
    val iconUrl: String,
)
