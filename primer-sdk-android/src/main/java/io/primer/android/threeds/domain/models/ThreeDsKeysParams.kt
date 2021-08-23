package io.primer.android.threeds.domain.models

import io.primer.android.model.dto.Environment
import io.primer.android.model.dto.ThreeDsSecureCertificate

internal data class ThreeDsKeysParams(
    val environment: Environment,
    val licenceKey: String?,
    val threeDsCertificates: List<ThreeDsSecureCertificate>?,
)
