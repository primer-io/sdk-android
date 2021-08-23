package io.primer.android.threeds.data.models

import kotlinx.serialization.Serializable

@Serializable
internal data class Authentication(
    val acsReferenceNumber: String? = null,
    val acsSignedContent: String? = null,
    val acsTransactionId: String? = null,
    val responseCode: ResponseCode,
    val transactionId: String?,
    val acsOperatorId: String?,
    val dsReferenceNumber: String? = null,
    val dsTransactionId: String? = null,
    val eci: String? = null,
    val protocolVersion: String? = null,
    // skipped
    val skippedReasonCode: SkippedCode? = null,
    val skippedReasonText: String? = null,
    // declined
    val declinedReasonCode: DeclinedReasonCode? = null,
    val declinedReasonText: String? = null,
)
