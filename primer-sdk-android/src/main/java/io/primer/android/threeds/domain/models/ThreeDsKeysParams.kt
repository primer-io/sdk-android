package io.primer.android.threeds.domain.models

import io.primer.android.data.configuration.models.Environment
import io.primer.android.data.configuration.models.ThreeDsSecureCertificateDataResponse

internal data class ThreeDsKeysParams(
    val environment: Environment,
    val licenceKey: String?,
    val threeDsCertificates: List<ThreeDsSecureCertificateDataResponse>?,
)
