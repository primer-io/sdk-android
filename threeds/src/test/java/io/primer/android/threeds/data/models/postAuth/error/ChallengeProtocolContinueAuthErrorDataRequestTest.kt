package io.primer.android.threeds.data.models.postAuth.error

import io.mockk.unmockkAll
import io.primer.android.threeds.data.models.postAuth.ThreeDsSdkErrorReasonCode
import org.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ChallengeProtocolContinueAuthErrorDataRequestTest {
    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `serializer should correctly serialize ChallengeProtocolContinueAuthErrorDataRequest`() {
        val reasonCode = ThreeDsSdkErrorReasonCode.`3DS_SDK_INIT_FAILED`
        val reasonText = "Transaction timed out."
        val threeDsErrorCode = "123"
        val threeDsErrorDescription = "Error description"
        val threeDsErrorComponent = "Error component"
        val threeDsErrorDetails = "Error details"
        val threeDsSdkTransactionId = "transaction-id"
        val protocolVersion = "2.1.0"

        val request =
            ChallengeProtocolContinueAuthErrorDataRequest(
                reasonCode,
                reasonText,
                threeDsErrorCode,
                threeDsErrorDescription,
                threeDsErrorComponent,
                threeDsErrorDetails,
                threeDsSdkTransactionId,
                protocolVersion,
            )

        val json = ChallengeProtocolContinueAuthErrorDataRequest.serializer.serialize(request)

        val expectedJson =
            JSONObject().apply {
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
