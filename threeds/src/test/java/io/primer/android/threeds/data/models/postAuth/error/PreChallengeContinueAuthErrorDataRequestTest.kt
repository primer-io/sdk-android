package io.primer.android.threeds.data.models.postAuth.error

import io.primer.android.threeds.data.models.postAuth.ThreeDsSdkErrorReasonCode
import io.mockk.unmockkAll
import org.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PreChallengeContinueAuthErrorDataRequestTest {

    @AfterEach
    fun tearDown() {
        unmockkAll()
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

        val json = PreChallengeContinueAuthErrorDataRequest.serializer.serialize(request)

        val expectedJson = JSONObject().apply {
            put("reasonCode", reasonCode.name)
            put("reasonText", reasonText)
            putOpt("recoverySuggestion", recoverySuggestion)
        }

        assertEquals(expectedJson.toString(), json.toString())
    }
}
