package io.primer.android.threeds.domain.models

import io.primer.android.configuration.data.model.Environment
import io.primer.android.configuration.data.model.ThreeDsSecureCertificateDataResponse

internal data class ThreeDsKeysParams(
    val environment: Environment,
    val apiKey: String?,
    val threeDsCertificates: List<ThreeDsSecureCertificateDataResponse>?,
)
