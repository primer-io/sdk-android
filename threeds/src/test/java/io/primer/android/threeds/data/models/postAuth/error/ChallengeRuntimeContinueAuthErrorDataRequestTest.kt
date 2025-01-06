package io.primer.android.threeds.data.models.postAuth.error

import io.mockk.unmockkAll
import io.primer.android.threeds.data.models.postAuth.ThreeDsSdkErrorReasonCode
import org.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ChallengeRuntimeContinueAuthErrorDataRequestTest {
    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `serializer should correctly serialize ChallengeRuntimeContinueAuthErrorDataRequest`() {
        val reasonCode = ThreeDsSdkErrorReasonCode.`3DS_SDK_INIT_FAILED`
        val reasonText = "Transaction timed out."
        val threeDsErrorCode = "123"
        val threeDsErrorDescription = "Error description"

        val request =
            ChallengeRuntimeContinueAuthErrorDataRequest(
                reasonCode,
                reasonText,
                threeDsErrorCode,
                threeDsErrorDescription,
            )

        val json = ChallengeRuntimeContinueAuthErrorDataRequest.serializer.serialize(request)

        val expectedJson =
            JSONObject().apply {
                put("reasonCode", reasonCode.name)
                put("reasonText", reasonText)
                putOpt("threeDsErrorCode", threeDsErrorCode)
                put("threeDsErrorDescription", threeDsErrorDescription)
            }

        assertEquals(expectedJson.toString(), json.toString())
    }
}
