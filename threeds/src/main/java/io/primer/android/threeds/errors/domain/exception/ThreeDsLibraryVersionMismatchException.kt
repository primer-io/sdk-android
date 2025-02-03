package io.primer.android.threeds.errors.domain.exception

internal class ThreeDsLibraryVersionMismatchException(
    val validSdkVersion: String,
    val threeDsWrapperSdkVersion: String,
    val threeDsSdkProvider: String,
) : IllegalStateException()
