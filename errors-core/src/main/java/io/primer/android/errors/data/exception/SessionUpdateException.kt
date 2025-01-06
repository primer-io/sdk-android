package io.primer.android.errors.data.exception

class SessionUpdateException(
    val description: String,
    val diagnosticsId: String?,
) : Exception()
