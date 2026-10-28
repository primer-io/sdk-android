package io.primer.checkout.orchestrator.data.datasource

import android.util.Base64
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import io.primer.checkout.orchestrator.data.verification.ManifestSignatureVerifier
import kotlinx.coroutines.test.runTest
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException

internal class ManifestRemoteDataSourceTest {

    private val okHttpClient = mockk<OkHttpClient>()
    private val signatureVerifier = mockk<ManifestSignatureVerifier>()
    private val dataSource = ManifestRemoteDataSource(okHttpClient, signatureVerifier)

    @BeforeEach
    fun setUp() {
        mockkStatic(Base64::class)
        every { Base64.decode(any<String>(), any()) } answers {
            java.util.Base64.getDecoder().decode(firstArg<String>())
        }
        every { signatureVerifier.verify(MANIFEST, SIGNATURE) } returns true
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Base64::class)
    }

    @Test
    fun `fetches the manifest again after a failed download`() = runTest {
        givenDownloadsFailing(times = 2)

        assertEquals("1.0.0", dataSource.fetchManifest(URL).stateProcessor.version)

        verify(exactly = 3) { okHttpClient.newCall(any()) }
    }

    @Test
    fun `gives up after three failed downloads`() = runTest {
        givenDownloadsFailing(times = 3)

        assertThrows<IOException> { dataSource.fetchManifest(URL) }

        verify(exactly = 3) { okHttpClient.newCall(any()) }
    }

    @Test
    fun `does not fetch again a manifest that fails verification`() = runTest {
        givenDownloadsFailing(times = 0)
        every { signatureVerifier.verify(any(), any()) } returns false

        assertThrows<SecurityException> { dataSource.fetchManifest(URL) }

        verify(exactly = 1) { okHttpClient.newCall(any()) }
    }

    private fun givenDownloadsFailing(times: Int) {
        var failures = 0
        val call = mockk<Call>()
        every { okHttpClient.newCall(any()) } returns call
        every { call.isCanceled() } returns false
        every { call.enqueue(any()) } answers {
            val callback = firstArg<Callback>()
            if (failures++ < times) {
                callback.onFailure(call, IOException("offline"))
            } else {
                callback.onResponse(call, ok())
            }
        }
    }

    private fun ok(): Response = Response.Builder()
        .request(Request.Builder().url(URL).build())
        .protocol(Protocol.HTTP_1_1)
        .code(200)
        .message("OK")
        .body(
            JSONObject()
                .put("signature", SIGNATURE)
                .put("manifest", java.util.Base64.getEncoder().encodeToString(MANIFEST.toByteArray()))
                .toString()
                .toResponseBody("application/json".toMediaType()),
        )
        .build()

    private companion object {
        const val URL = "https://sdk.primer.io/state-processor/v0/manifests/sandbox.json"
        const val SIGNATURE = "signature"
        const val MANIFEST = """
            {
                "stateProcessor": {
                    "version": "1.0.0",
                    "umd": {
                        "url": "https://example.com/sp.js",
                        "sha256": "sp",
                        "gz": "https://example.com/sp.js.gz"
                    }
                },
                "cel": {
                    "version": "1.0.0",
                    "noModules": {
                        "js": {
                            "url": "https://example.com/cel.js",
                            "sha256": "js",
                            "gz": "https://example.com/cel.js.gz"
                        },
                        "wasm": {
                            "url": "https://example.com/cel.wasm",
                            "sha256": "wasm",
                            "gz": "https://example.com/cel.wasm.gz"
                        }
                    }
                }
            }
        """
    }
}
