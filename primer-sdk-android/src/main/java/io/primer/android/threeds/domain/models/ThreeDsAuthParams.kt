package io.primer.android.threeds.domain.models

import io.primer.android.data.configuration.models.Environment
import io.primer.android.threeds.helpers.ProtocolVersion

internal data class ThreeDsAuthParams(
    val environment: Environment,
    val protocolVersions: List<ProtocolVersion>
)
