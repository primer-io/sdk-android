package io.primer.checkout.orchestrator.data.verification

import android.util.Base64
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

/**
 * Verifies ECDSA P-256 / SHA-256 signatures on state processor manifests.
 *
 * @param publicKeysB64 Base64-encoded SPKI/DER public keys to try. Verification
 *   succeeds if *any* key produces a valid signature (allows key rotation).
 */
internal class ManifestSignatureVerifier(
    private val publicKeysB64: List<String>,
) {

    fun verify(manifestText: String, signatureB64: String): Boolean {
        val signatureBytes = Base64.decode(signatureB64, Base64.DEFAULT)
        val data = manifestText.toByteArray(Charsets.UTF_8)

        for (keyB64 in publicKeysB64) {
            try {
                val keyBytes = Base64.decode(keyB64, Base64.DEFAULT)
                val keySpec = X509EncodedKeySpec(keyBytes)
                val publicKey = KeyFactory.getInstance(EC_ALGORITHM).generatePublic(keySpec)

                val sig = Signature.getInstance(SIGNATURE_ALGORITHM)
                sig.initVerify(publicKey)
                sig.update(data)

                if (sig.verify(signatureBytes)) {
                    return true
                }
            } catch (_: Exception) {
                // Key import or verification failed — try next key.
            }
        }

        return false
    }

    private companion object {
        const val EC_ALGORITHM = "EC"
        const val SIGNATURE_ALGORITHM = "SHA256withECDSA"
    }
}
