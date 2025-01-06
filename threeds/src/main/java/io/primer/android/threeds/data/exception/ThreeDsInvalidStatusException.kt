package io.primer.android.threeds.data.exception

import io.primer.android.analytics.domain.models.ThreeDsRuntimeFailureContextParams
import kotlinx.coroutines.CancellationException

internal class ThreeDsInvalidStatusException(
    val transactionStatus: String,
    val transactionId: String,
    val errorCode: String,
    val context: ThreeDsRuntimeFailureContextParams,
    override val message: String?,
) : CancellationException()
