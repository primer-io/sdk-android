package io.primer.android.threeds.data.models.common

import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.extensions.optNullableString
import io.primer.android.payments.core.tokenization.data.model.ResponseCode

internal data class AuthenticationDataResponse(
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
    val declinedReasonText: String? = null
) : JSONDeserializable {

    companion object {
        private const val ACS_REFERENCE_NUMBER_FIELD = "acsReferenceNumber"
        private const val ACS_SIGNED_CONTENT_FIELD = "acsSignedContent"
        private const val ACS_TRANSACTION_ID_FIELD = "acsTransactionId"
        private const val RESPONSE_CODE_FIELD = "responseCode"
        private const val TRANSACTION_ID_FIELD = "transactionId"
        private const val ACS_OPERATOR_ID_FIELD = "acsOperatorId"
        private const val DS_REFERENCE_NUMBER_FIELD = "dsReferenceNumber"
        private const val DS_TRANSACTION_ID_FIELD = "dsTransactionId"
        private const val ECI_FIELD = "eci"
        private const val PROTOCOL_VERSION_FIELD = "protocolVersion"
        private const val SKIPPED_REASON_CODE_FIELD = "skippedReasonCode"
        private const val SKIPPED_REASON_TEXT_FIELD = "skippedReasonText"
        private const val DECLINED_REASON_CODE_FIELD = "declinedReasonCode"
        private const val DECLINED_REASON_TEXT_FIELD = "declinedReasonText"

        @JvmField
        val deserializer = JSONObjectDeserializer { t ->
            AuthenticationDataResponse(
                acsReferenceNumber = t.optNullableString(ACS_REFERENCE_NUMBER_FIELD),
                acsSignedContent = t.optNullableString(ACS_SIGNED_CONTENT_FIELD),
                acsTransactionId = t.optNullableString(ACS_TRANSACTION_ID_FIELD),
                responseCode = ResponseCode.valueOf(t.getString(RESPONSE_CODE_FIELD)),
                transactionId = t.optNullableString(TRANSACTION_ID_FIELD),
                acsOperatorId = t.optNullableString(ACS_OPERATOR_ID_FIELD),
                dsReferenceNumber = t.optNullableString(DS_REFERENCE_NUMBER_FIELD),
                dsTransactionId = t.optNullableString(DS_TRANSACTION_ID_FIELD),
                eci = t.optNullableString(ECI_FIELD),
                protocolVersion = t.optNullableString(PROTOCOL_VERSION_FIELD),
                skippedReasonCode = t.optNullableString(SKIPPED_REASON_CODE_FIELD)?.let { SkippedCode.valueOf(it) },
                skippedReasonText = t.optNullableString(SKIPPED_REASON_TEXT_FIELD),
                declinedReasonCode = t.optNullableString(DECLINED_REASON_CODE_FIELD)
                    ?.let { DeclinedReasonCode.valueOf(it) },
                declinedReasonText = t.optNullableString(DECLINED_REASON_TEXT_FIELD)
            )
        }
    }
}
