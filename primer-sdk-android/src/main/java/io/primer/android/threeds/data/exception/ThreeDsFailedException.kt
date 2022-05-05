package io.primer.android.threeds.data.exception

import java.util.concurrent.CancellationException

internal class ThreeDsFailedException(
    val errorCode: String? = null,
    override val message: String?
) : CancellationException()
