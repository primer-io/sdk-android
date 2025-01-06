package io.primer.android.threeds.data.models.postAuth

import io.mockk.unmockkAll
import io.primer.android.threeds.BuildConfig
import io.primer.android.threeds.domain.models.SuccessThreeDsContinueAuthParams
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class SuccessContinueAuthDataRequestTest {
    @AfterEach
    fun tearDown() {
        // Unmock all mocked objects
        unmockkAll()
    }

    @Test
    fun `serializer should correctly serialize SuccessContinueAuthDataRequest`() {
        // Create real values
        val threeDsSdkVersion = "2.2.0"
        val initProtocolVersion = "2.1.0"
        val threeDsWrapperSdkVersion = "1.0.0"
        val threeDsSdkProvider = ThreeDsSdkProvider.NETCETERA

        val successRequest =
            SuccessContinueAuthDataRequest(
                threeDsSdkVersion = threeDsSdkVersion,
                initProtocolVersion = initProtocolVersion,
                threeDsWrapperSdkVersion = threeDsWrapperSdkVersion,
                threeDsSdkProvider = threeDsSdkProvider,
            )

        // Serialize the object
        val jsonObject = SuccessContinueAuthDataRequest.serializer.serialize(successRequest)

        // Assertions to verify correctness
        assertEquals(initProtocolVersion, jsonObject.getString(BaseContinueAuthDataRequest.INIT_PROTOCOL_VERSION_FIELD))
        assertEquals(
            threeDsWrapperSdkVersion,
            jsonObject.getString(BaseContinueAuthDataRequest.SDK_WRAPPER_VERSION_FIELD),
        )
        assertEquals(threeDsSdkVersion, jsonObject.optString(BaseContinueAuthDataRequest.SDK_VERSION_FIELD))
        assertEquals(threeDsSdkProvider.name, jsonObject.getString(BaseContinueAuthDataRequest.SDK_PROVIDER_FIELD))
    }

    @Test
    fun `toContinueAuthDataRequest should correctly convert SuccessThreeDsContinueAuthParams`() {
        // Create a SuccessThreeDsContinueAuthParams object
        val threeDsSdkVersion = "2.2.0"
        val initProtocolVersion = "2.1.0"
        val successParams =
            SuccessThreeDsContinueAuthParams(
                threeDsSdkVersion = threeDsSdkVersion,
                initProtocolVersion = initProtocolVersion,
            )

        // Convert to SuccessContinueAuthDataRequest
        val successRequest = successParams.toContinueAuthDataRequest()

        // Assertions to verify correctness
        assertEquals(threeDsSdkVersion, successRequest.threeDsSdkVersion)
        assertEquals(initProtocolVersion, successRequest.initProtocolVersion)
        assertEquals(BuildConfig.SDK_VERSION_STRING, successRequest.threeDsWrapperSdkVersion)
        assertEquals(ThreeDsSdkProvider.NETCETERA, successRequest.threeDsSdkProvider)
    }
}
