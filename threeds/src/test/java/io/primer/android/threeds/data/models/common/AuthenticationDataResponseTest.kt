package io.primer.android.threeds.data.models.common

import io.primer.android.payments.core.tokenization.data.model.ResponseCode
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AuthenticationDataResponseTest {

    @Test
    fun `deserializer should deserialize JSONObject to AuthenticationDataResponse correctly`() {
        // Define the repeating values
        val acsReferenceNumber = "acsRef123"
        val acsSignedContent = "signedContent123"
        val acsTransactionId = "trans123"
        val responseCode = ResponseCode.AUTH_SUCCESS
        val transactionId = "transId123"
        val acsOperatorId = "operator123"
        val dsReferenceNumber = "dsRef123"
        val dsTransactionId = "dsTrans123"
        val eci = "eci123"
        val protocolVersion = "2.0"
        val skippedReasonCode = SkippedCode.`3DS_SERVER_ERROR`
        val skippedReasonText = "Cardholder not participating"
        val declinedReasonCode = DeclinedReasonCode.CARD_AUTHENTICATION_FAILED
        val declinedReasonText = "Authentication failed"

        // Create a JSONObject with these values
        val jsonObject = JSONObject().apply {
            putOpt("acsReferenceNumber", acsReferenceNumber)
            putOpt("acsSignedContent", acsSignedContent)
            putOpt("acsTransactionId", acsTransactionId)
            put("responseCode", responseCode.name)
            putOpt("transactionId", transactionId)
            putOpt("acsOperatorId", acsOperatorId)
            putOpt("dsReferenceNumber", dsReferenceNumber)
            putOpt("dsTransactionId", dsTransactionId)
            putOpt("eci", eci)
            putOpt("protocolVersion", protocolVersion)
            putOpt("skippedReasonCode", skippedReasonCode.name)
            putOpt("skippedReasonText", skippedReasonText)
            putOpt("declinedReasonCode", declinedReasonCode.name)
            putOpt("declinedReasonText", declinedReasonText)
        }

        // Deserialize the JSONObject to an instance of AuthenticationDataResponse
        val authDataResponse = AuthenticationDataResponse.deserializer.deserialize(jsonObject)

        // Create an expected AuthenticationDataResponse instance
        val expectedAuthDataResponse = AuthenticationDataResponse(
            acsReferenceNumber = acsReferenceNumber,
            acsSignedContent = acsSignedContent,
            acsTransactionId = acsTransactionId,
            responseCode = responseCode,
            transactionId = transactionId,
            acsOperatorId = acsOperatorId,
            dsReferenceNumber = dsReferenceNumber,
            dsTransactionId = dsTransactionId,
            eci = eci,
            protocolVersion = protocolVersion,
            skippedReasonCode = skippedReasonCode,
            skippedReasonText = skippedReasonText,
            declinedReasonCode = declinedReasonCode,
            declinedReasonText = declinedReasonText
        )

        // Assert the deserialized AuthenticationDataResponse matches the expected values
        assertEquals(expectedAuthDataResponse, authDataResponse)
    }

    @Test
    fun `deserializer should handle null values correctly`() {
        // Create a JSONObject with null values
        val jsonObject = JSONObject().apply {
            putOpt("acsReferenceNumber", null)
            putOpt("acsSignedContent", null)
            putOpt("acsTransactionId", null)
            put("responseCode", ResponseCode.AUTH_SUCCESS.name)
            putOpt("transactionId", null)
            putOpt("acsOperatorId", null)
            putOpt("dsReferenceNumber", null)
            putOpt("dsTransactionId", null)
            putOpt("eci", null)
            putOpt("protocolVersion", null)
            putOpt("skippedReasonCode", null)
            putOpt("skippedReasonText", null)
            putOpt("declinedReasonCode", null)
            putOpt("declinedReasonText", null)
        }

        // Deserialize the JSONObject to an instance of AuthenticationDataResponse
        val authDataResponse = AuthenticationDataResponse.deserializer.deserialize(jsonObject)

        // Create an expected AuthenticationDataResponse instance with null values
        val expectedAuthDataResponse = AuthenticationDataResponse(
            acsReferenceNumber = null,
            acsSignedContent = null,
            acsTransactionId = null,
            responseCode = ResponseCode.AUTH_SUCCESS,
            transactionId = null,
            acsOperatorId = null,
            dsReferenceNumber = null,
            dsTransactionId = null,
            eci = null,
            protocolVersion = null,
            skippedReasonCode = null,
            skippedReasonText = null,
            declinedReasonCode = null,
            declinedReasonText = null
        )

        // Assert the deserialized AuthenticationDataResponse matches the expected values
        assertEquals(expectedAuthDataResponse, authDataResponse)
    }
}
