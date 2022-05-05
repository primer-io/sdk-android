package io.primer.android.data.configuration.model

import kotlinx.serialization.Serializable

@Serializable
internal data class ConfigurationKeys(
    internal val threeDSecureIoCertificates: List<ThreeDsSecureCertificate>? = null,
    internal val netceteraLicenseKey: String? = null,
)

@Serializable
internal data class ThreeDsSecureCertificate(
    val cardNetwork: String,
    val rootCertificate: String,
    val encryptionKey: String,
)
