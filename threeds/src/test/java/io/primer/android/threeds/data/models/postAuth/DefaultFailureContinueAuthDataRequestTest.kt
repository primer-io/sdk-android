import io.primer.android.threeds.BuildConfig
import io.primer.android.threeds.data.models.postAuth.DefaultFailureContinueAuthDataRequest
import io.primer.android.threeds.data.models.postAuth.ThreeDsSdkErrorReasonCode
import io.primer.android.threeds.data.models.postAuth.ThreeDsSdkProvider
import io.primer.android.threeds.data.models.postAuth.error.PreChallengeContinueAuthErrorDataRequest
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DefaultFailureContinueAuthDataRequestTest {

    @Test
    fun `serializer should correctly serialize DefaultFailureContinueAuthDataRequest`() {
        // Arrange
        val error = PreChallengeContinueAuthErrorDataRequest(
            reasonCode = ThreeDsSdkErrorReasonCode.MISSING_SDK_DEPENDENCY,
            reasonText = "SDK is missing",
            recoverySuggestion = "Please include the SDK"
        )

        val request = DefaultFailureContinueAuthDataRequest(
            threeDsSdkVersion = "2.2.0",
            initProtocolVersion = "2.1.0",
            error = error
        )

        val expectedJson = JSONObject()
            .putOpt("initProtocolVersion", "2.1.0")
            .put("threeDsWrapperSdkVersion", BuildConfig.SDK_VERSION_STRING)
            .put("threeDsSdkProvider", ThreeDsSdkProvider.NETCETERA.name)
            .put(
                "error",
                JSONObject().apply {
                    put("reasonCode", ThreeDsSdkErrorReasonCode.MISSING_SDK_DEPENDENCY.name)
                    put("reasonText", "SDK is missing")
                    put("recoverySuggestion", "Please include the SDK")
                }
            )
            .putOpt("platform", "ANDROID_NATIVE")
            .putOpt("status", "FAILURE")
            .putOpt("threeDsSdkVersion", "2.2.0")

        // Act
        val serializedJson = DefaultFailureContinueAuthDataRequest.serializer.serialize(request)

        // Assert
        assertEquals(expectedJson.toString(), serializedJson.toString())
    }
}
