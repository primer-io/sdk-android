package io.primer.android.threeds.data.models.postAuth

import io.primer.android.threeds.data.models.postAuth.error.PreChallengeContinueAuthErrorDataRequest
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class MissingDependencyFailureContinueAuthDataRequestTest {
    @Test
    fun `test that the serialization works correctly`() {
        // Prepare the error object with real values
        val error =
            PreChallengeContinueAuthErrorDataRequest(
                reasonCode = ThreeDsSdkErrorReasonCode.MISSING_SDK_DEPENDENCY,
                reasonText = "SDK is missing",
                recoverySuggestion = "Please include the SDK",
            )

        // Create an instance of MissingDependencyFailureContinueAuthDataRequest
        val request = MissingDependencyFailureContinueAuthDataRequest(error = error)

        // Serialize the request
        val json = MissingDependencyFailureContinueAuthDataRequest.serializer.serialize(request)

        // Verify the JSON output
        val expectedJson =
            JSONObject().apply {
                put(
                    "error",
                    JSONObject().apply {
                        put("reasonCode", ThreeDsSdkErrorReasonCode.MISSING_SDK_DEPENDENCY.name)
                        put("reasonText", "SDK is missing")
                        put("recoverySuggestion", "Please include the SDK")
                    },
                )
                put("status", "FAILURE")
                put("platform", "ANDROID_NATIVE")
            }

        assertEquals(expectedJson.toString(), json.toString())
    }
}
