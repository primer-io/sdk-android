package io.primer.android.domain.exception

import io.primer.android.analytics.domain.models.ThreeDsFailureContextParams

internal class ThreeDsLibraryVersionMismatchException(
    val validSdkVersion: String,
    val context: ThreeDsFailureContextParams
) : IllegalStateException()
