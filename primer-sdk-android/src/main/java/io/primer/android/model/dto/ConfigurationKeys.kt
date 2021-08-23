package io.primer.android.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class ConfigurationKeys(
    internal val threeDSecureIoCertificates: List<ThreeDsSecureCertificate>? = null,
    internal val netceteraLicenseKey: String? = null,
)

@Serializable
data class ThreeDsSecureCertificate(
    val cardNetwork: String,
    val rootCertificate: String,
    val encryptionKey: String,
)
