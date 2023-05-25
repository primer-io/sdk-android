package io.primer.android.threeds.data.exception

import io.primer.android.analytics.domain.models.ThreeDsProtocolFailureContextParams
import java.util.concurrent.CancellationException

internal class ThreeDsProtocolFailedException(
    val errorCode: String,
    val context: ThreeDsProtocolFailureContextParams,
    override val message: String
) : CancellationException()
