package io.primer.android.threeds.data.exception

import java.util.concurrent.CancellationException

internal class ThreeDsProtocolFailedException(
    override val message: String,
    val threeDsSdkVersion: String?,
    val initProtocolVersion: String,
    val threeDsWrapperSdkVersion: String,
    val threeDsSdkProvider: String,
    val errorDetails: String,
    val description: String,
    val errorCode: String,
    val messageType: String,
    val component: String,
    val transactionId: String,
    val version: String,
) : CancellationException()
