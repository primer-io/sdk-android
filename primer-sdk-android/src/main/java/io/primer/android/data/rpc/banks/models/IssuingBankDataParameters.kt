package io.primer.android.data.rpc.banks.models

import kotlinx.serialization.Serializable

@Serializable
internal data class IssuingBankDataParameters(
    val paymentMethod: String,
    val locale: String
)
