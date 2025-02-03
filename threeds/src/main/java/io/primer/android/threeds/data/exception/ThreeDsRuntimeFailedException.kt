package io.primer.android.threeds.data.exception

import java.util.concurrent.CancellationException

internal class ThreeDsRuntimeFailedException(
    override val message: String?,
    val threeDsSdkVersion: String?,
    val initProtocolVersion: String,
    val threeDsWrapperSdkVersion: String,
    val threeDsSdkProvider: String,
    val errorCode: String,
) : CancellationException()
