package io.primer.android.threeds.data.exception

internal class ThreeDsInitException(
    override val message: String?,
    val threeDsSdkVersion: String?,
    val threeDsWrapperSdkVersion: String,
    val threeDsSdkProvider: String,
) : IllegalStateException()
