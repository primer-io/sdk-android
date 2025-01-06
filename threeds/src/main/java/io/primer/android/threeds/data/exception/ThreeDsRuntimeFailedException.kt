package io.primer.android.threeds.data.exception

import io.primer.android.analytics.domain.models.ThreeDsRuntimeFailureContextParams
import java.util.concurrent.CancellationException

internal class ThreeDsRuntimeFailedException(
    val context: ThreeDsRuntimeFailureContextParams,
    override val message: String?,
) : CancellationException()
