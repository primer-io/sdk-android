package io.primer.android.threeds.data.exception

import io.primer.android.analytics.domain.models.ThreeDsRuntimeFailureContextParams
import java.util.concurrent.CancellationException

internal class ThreeDsChallengeCancelledException(
    val errorCode: String,
    val context: ThreeDsRuntimeFailureContextParams,
    override val message: String?,
) : CancellationException()
