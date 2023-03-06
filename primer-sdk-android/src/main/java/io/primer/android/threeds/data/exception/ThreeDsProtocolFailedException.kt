package io.primer.android.threeds.data.exception

import io.primer.android.analytics.domain.models.ThreeDsFailureContextParams
import java.util.concurrent.CancellationException

internal class ThreeDsProtocolFailedException(
    val errorCode: String,
    val context: ThreeDsFailureContextParams,
    override val message: String
) : CancellationException()
