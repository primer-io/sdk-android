package io.primer.android.threeds.data.models

import kotlinx.serialization.Serializable

@Serializable
internal data class SDKAuthData(
    val sdkAppId: String,
    val sdkTransactionId: String,
    val sdkTimeout: Int,
    val sdkEncData: String,
    val sdkEphemPubKey: String,
    val sdkReferenceNumber: String,
)
