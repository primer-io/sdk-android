package io.primer.checkout.orchestrator.data.datasource

import android.util.Base64
import io.primer.android.core.data.network.extensions.await
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.checkout.orchestrator.ManifestOverrides
import io.primer.checkout.orchestrator.data.model.ManifestResponse
import io.primer.checkout.orchestrator.data.model.StateProcessorManifest
import io.primer.checkout.orchestrator.data.verification.ManifestSignatureVerifier
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlin.time.Duration.Companion.milliseconds

internal class ManifestRemoteDataSource(
    private val okHttpClient: OkHttpClient,
    private val signatureVerifier: ManifestSignatureVerifier,
) {

    suspend fun fetchManifest(manifestUrl: String): StateProcessorManifest {
        val manifestResponse = fetchWithRetry(manifestUrl = manifestUrl)
        val manifestText = String(Base64.decode(manifestResponse.manifest, Base64.DEFAULT), Charsets.UTF_8)

        if (!signatureVerifier.verify(manifestText, manifestResponse.signature)) {
            throw SecurityException("Manifest signature verification failed.")
        }

        return StateProcessorManifest.deserializer.deserialize(JSONObject(manifestText))
    }

    // Like the bundle's resources; a manifest that fails verification is not fetched again.
    private suspend fun fetchWithRetry(manifestUrl: String): ManifestResponse {
        repeat(MAX_RETRIES - 1) {
            runSuspendCatching { return fetch(manifestUrl) }
            delay(RETRY_DELAY_MS.milliseconds)
        }
        return fetch(manifestUrl)
    }

    private suspend fun fetch(manifestUrl: String): ManifestResponse {
        val request = Request.Builder()
            .url(manifestUrl)
            .get()
            .build()

        val response = okHttpClient.newCall(request).await()
        val body = response.body ?: error("Missing manifest response body.")

        check(response.isSuccessful) {
            "Failed to fetch state processor manifest: ${response.code}"
        }

        return ManifestResponse.deserializer.deserialize(JSONObject(body.string()))
    }

    companion object {
        private const val BASE_MANIFEST_URL = "https://sdk.primer.io/state-processor/v0/manifests"
        private const val MAX_RETRIES = 3
        private const val RETRY_DELAY_MS = 500L

        fun buildManifestUrl(environment: String): String =
            "$BASE_MANIFEST_URL/$environment.json"

        /** The environment's manifest, unless a debug build overrides it. */
        fun manifestUrl(environment: String): String = ManifestOverrides.url ?: buildManifestUrl(environment)
    }
}
