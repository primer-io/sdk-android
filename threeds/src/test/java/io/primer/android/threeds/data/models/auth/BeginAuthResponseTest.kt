package io.primer.android.threeds.data.models.auth

import io.primer.android.data.tokenization.models.TokenType
import io.primer.android.payments.core.tokenization.data.model.PaymentMethodTokenInternal
import io.primer.android.payments.core.tokenization.data.model.ResponseCode
import io.primer.android.threeds.data.models.common.AuthenticationDataResponse
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BeginAuthResponseTest {
    @Test
    fun `deserializer should deserialize BeginAuthResponse from JSONObject correctly`() {
        // Define the repeating values
        val analyticsId = "analytics-id"
        val tokenValue = "token-value"
        val acsTransId = "acs-trans-id"
        val acsRefNo = "acs-ref-no"
        val acsUrl = "https://acs.url"
        val transactionStatus = "Y"
        val paymentMethodType = "paymentMethodType"
        val paymentInstrumentType = "paymentInstrumentType"
        val paymentInstrumentData = "paymentInstrumentData"
        val resumeTokenValue = "resume-token-value"

        // Create JSON objects with these values
        val tokenJson =
            JSONObject().apply {
                put("token", tokenValue)
                put("paymentMethodType", paymentMethodType)
                put("paymentInstrumentType", paymentInstrumentType)
                put("paymentInstrumentData", paymentInstrumentData)
                put("analyticsId", analyticsId)
                put("tokenType", TokenType.SINGLE_USE.name)
            }

        val authenticationJson =
            JSONObject().apply {
                put("acsReferenceNumber", acsRefNo)
                put("acsTransId", acsTransId)
                put("acsUrl", acsUrl)
                put("transactionStatus", transactionStatus)
                put("responseCode", ResponseCode.AUTH_SUCCESS.name)
            }

        val responseJson =
            JSONObject().apply {
                put("token", tokenJson)
                put("authentication", authenticationJson)
                put("resumeToken", resumeTokenValue)
            }

        // Create expected objects with the same values
        val token =
            PaymentMethodTokenInternal(
                token = tokenValue,
                paymentInstrumentType = paymentInstrumentType,
                paymentMethodType = paymentInstrumentType,
                paymentInstrumentData = null,
                vaultData = null,
                threeDSecureAuthentication = null,
                isVaulted = false,
                analyticsId = analyticsId,
                tokenType = TokenType.SINGLE_USE,
            )

        val authentication =
            AuthenticationDataResponse(
                acsReferenceNumber = acsRefNo,
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
            )

        // Deserialize the JSON object
        val beginAuthResponse = BeginAuthResponse.deserializer.deserialize(responseJson)

        // Assert the deserialized object matches the expected values
        assertEquals(token, beginAuthResponse.token)
        assertEquals(authentication, beginAuthResponse.authentication)
        assertEquals(resumeTokenValue, beginAuthResponse.resumeToken)
    }
}
