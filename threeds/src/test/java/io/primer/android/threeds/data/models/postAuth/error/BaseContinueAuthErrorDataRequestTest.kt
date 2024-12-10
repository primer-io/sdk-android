package io.primer.android.threeds.data.models.postAuth.error

import io.primer.android.threeds.data.models.postAuth.ThreeDsSdkErrorReasonCode
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class BaseContinueAuthErrorDataRequestTest {

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `baseSerializer should correctly serialize BaseContinueAuthErrorDataRequest`() {
        val reasonCode = ThreeDsSdkErrorReasonCode.`3DS_SDK_INIT_FAILED`
        val reasonText = "Transaction timed out."

        val mockRequest = mockk<BaseContinueAuthErrorDataRequest> {
            every { this@mockk.reasonCode } returns reasonCode
            every { this@mockk.reasonText } returns reasonText
        }

        val json = BaseContinueAuthErrorDataRequest.baseSerializer.serialize(mockRequest)

        val expectedJson = JSONObject().apply {
            put("reasonCode", reasonCode.name)
            put("reasonText", reasonText)
        }

        assertEquals(expectedJson.toString(), json.toString())
    }

    @Test
    fun `serializer should correctly serialize PreChallengeContinueAuthErrorDataRequest`() {
        val reasonCode = ThreeDsSdkErrorReasonCode.`3DS_SDK_INIT_FAILED`
        val reasonText = "Transaction timed out."
        val recoverySuggestion = "Try again later."

        val request = PreChallengeContinueAuthErrorDataRequest(
            reasonCode,
            reasonText,
            recoverySuggestion
        )

        val json = BaseContinueAuthErrorDataRequest.serializer.serialize(request)

        val expectedJson = JSONObject().apply {
            put("reasonCode", reasonCode.name)
            put("reasonText", reasonText)
            putOpt("recoverySuggestion", recoverySuggestion)
        }

        assertEquals(expectedJson.toString(), json.toString())
    }

    @Test
    fun `serializer should correctly serialize ChallengeRuntimeContinueAuthErrorDataRequest`() {
        val reasonCode = ThreeDsSdkErrorReasonCode.`3DS_SDK_INIT_FAILED`
        val reasonText = "Transaction timed out."
        val threeDsErrorCode = "errorCode"
        val threeDsErrorDescription = "errorDescription"

        val request = ChallengeRuntimeContinueAuthErrorDataRequest(
            reasonCode,
            reasonText,
            threeDsErrorCode,
            threeDsErrorDescription
        )

        val json = BaseContinueAuthErrorDataRequest.serializer.serialize(request)

        val expectedJson = JSONObject().apply {
            put("reasonCode", reasonCode.name)
            put("reasonText", reasonText)
            putOpt("threeDsErrorCode", threeDsErrorCode)
            put("threeDsErrorDescription", threeDsErrorDescription)
        }

        assertEquals(expectedJson.toString(), json.toString())
    }

    @Test
    fun `serializer should correctly serialize ChallengeProtocolContinueAuthErrorDataRequest`() {
        val reasonCode = ThreeDsSdkErrorReasonCode.`3DS_SDK_INIT_FAILED`
        val reasonText = "Transaction timed out."
        val threeDsErrorCode = "errorCode"
        val threeDsErrorDescription = "errorDescription"
        val threeDsErrorComponent = "errorComponent"
        val threeDsErrorDetails = "errorDetails"
        val threeDsSdkTransactionId = "transactionId"
        val protocolVersion = "protocolVersion"

        val request = ChallengeProtocolContinueAuthErrorDataRequest(
            reasonCode,
            reasonText,
            threeDsErrorCode,
            threeDsErrorDescription,
            threeDsErrorComponent,
            threeDsErrorDetails,
            threeDsSdkTransactionId,
            protocolVersion
        )

        val json = BaseContinueAuthErrorDataRequest.serializer.serialize(request)

        val expectedJson = JSONObject().apply {
            put("reasonCode", reasonCode.name)
            put("reasonText", reasonText)
            putOpt("threeDsErrorCode", threeDsErrorCode)
            put("threeDsErrorDescription", threeDsErrorDescription)
            put("threeDsErrorComponent", threeDsErrorComponent)
            put("threeDsErrorDetails", threeDsErrorDetails)
            put("threeDsSdkTransactionId", threeDsSdkTransactionId)
            put("protocolVersion", protocolVersion)
        }

        assertEquals(expectedJson.toString(), json.toString())
    }
}
