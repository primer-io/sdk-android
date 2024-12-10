package io.primer.android.threeds.domain.models

import io.primer.android.core.domain.Params
import io.primer.android.domain.error.models.PrimerError

internal sealed class BaseThreeDsContinueAuthParams : Params

internal data class SuccessThreeDsContinueAuthParams(
    val threeDsSdkVersion: String?,
    val initProtocolVersion: String
) :
    BaseThreeDsContinueAuthParams()

internal data class FailureThreeDsContinueAuthParams(
    val threeDsSdkVersion: String? = null,
    val initProtocolVersion: String? = null,
    val error: PrimerError
) : BaseThreeDsContinueAuthParams()
