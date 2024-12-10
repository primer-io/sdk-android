package io.primer.android.threeds.data.exception

import io.primer.android.analytics.domain.models.ThreeDsFailureContextParams

internal class ThreeDsInitException(
    override val message: String?,
    val context: ThreeDsFailureContextParams
) : IllegalStateException()
