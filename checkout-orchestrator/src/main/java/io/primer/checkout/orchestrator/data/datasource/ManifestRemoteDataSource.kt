package io.primer.checkout.orchestrator.data.datasource

import android.util.Base64
import io.primer.android.core.data.network.extensions.await
import io.primer.checkout.orchestrator.data.model.ManifestResponse
import io.primer.checkout.orchestrator.data.model.StateProcessorManifest
import io.primer.checkout.orchestrator.data.verification.ManifestSignatureVerifier
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

internal class ManifestRemoteDataSource(
    private val okHttpClient: OkHttpClient,
    private val signatureVerifier: ManifestSignatureVerifier,
) {

    suspend fun fetchManifest(manifestUrl: String): StateProcessorManifest {
        val request = Request.Builder()
            .url(manifestUrl)
            .get()
            .build()

        val response = okHttpClient.newCall(request).await()
        val body = response.body ?: error("Missing manifest response body.")

        check(response.isSuccessful) {
            "Failed to fetch state processor manifest: ${response.code}"
        }

        val manifestResponse = ManifestResponse.deserializer.deserialize(JSONObject(body.string()))
        val manifestText = String(Base64.decode(manifestResponse.manifest, Base64.DEFAULT), Charsets.UTF_8)

        if (!signatureVerifier.verify(manifestText, manifestResponse.signature)) {
            throw SecurityException("Manifest signature verification failed.")
        }

        return StateProcessorManifest.deserializer.deserialize(JSONObject(manifestText))
    }

    companion object {
        private const val BASE_MANIFEST_URL = "https://sdk.primer.io/state-processor/v0/manifests"

        fun buildManifestUrl(environment: String): String =
            "$BASE_MANIFEST_URL/$environment.json"
    }
}
