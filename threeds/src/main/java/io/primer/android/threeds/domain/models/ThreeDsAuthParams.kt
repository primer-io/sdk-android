package io.primer.android.threeds.domain.models

import io.primer.android.configuration.data.model.Environment
import io.primer.android.threeds.helpers.ProtocolVersion

internal data class ThreeDsAuthParams(
    val environment: Environment,
    val protocolVersions: List<ProtocolVersion>
)
