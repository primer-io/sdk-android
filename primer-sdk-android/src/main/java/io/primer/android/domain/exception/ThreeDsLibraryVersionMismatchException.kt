package io.primer.android.domain.exception

internal class ThreeDsLibraryVersionMismatchException(val validSdkVersion: String) :
    IllegalStateException()
