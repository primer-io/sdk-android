package io.primer.android.threeds.data.exception

import kotlinx.coroutines.CancellationException

internal class ThreeDsInvalidStatusException(
    override val message: String?,
    val transactionStatus: String,
    val transactionId: String,
    val threeDsSdkVersion: String?,
    val initProtocolVersion: String,
    val threeDsWrapperSdkVersion: String,
    val threeDsSdkProvider: String,
    val errorCode: String,
) : CancellationException()
