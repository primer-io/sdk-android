package io.primer.android.threeds.domain.models

import io.primer.android.data.configuration.model.Environment
import io.primer.android.data.configuration.model.ThreeDsSecureCertificate

internal data class ThreeDsKeysParams(
    val environment: Environment,
    val licenceKey: String?,
    val threeDsCertificates: List<ThreeDsSecureCertificate>?,
)
