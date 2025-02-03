package io.primer.android.threeds.data.exception

internal class ThreeDsConfigurationException(
    override val message: String?,
    val threeDsWrapperSdkVersion: String,
    val threeDsSdkProvider: String,
) : IllegalArgumentException()
