package io.primer.android.threeds.data.exception

internal class ThreeDsUnknownProtocolException(
    val initProtocolVersion: String,
    val threeDsWrapperSdkVersion: String,
    val threeDsSdkProvider: String,
) :
    IllegalStateException()
