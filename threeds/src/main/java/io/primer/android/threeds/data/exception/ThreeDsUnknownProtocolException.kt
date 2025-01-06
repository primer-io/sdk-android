package io.primer.android.threeds.data.exception

import io.primer.android.analytics.domain.models.ThreeDsFailureContextParams

internal class ThreeDsUnknownProtocolException(
    val initProtocolVersion: String,
    val context: ThreeDsFailureContextParams,
) :
    IllegalStateException()
