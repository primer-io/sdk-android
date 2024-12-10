package io.primer.android.threeds.errors.domain.exception

import io.primer.android.analytics.domain.models.ThreeDsFailureContextParams

internal class ThreeDsLibraryVersionMismatchException(
    val validSdkVersion: String,
    val context: ThreeDsFailureContextParams
) : IllegalStateException()
