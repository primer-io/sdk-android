package io.primer.android.http

internal data class PrimerResponse<R>(
    val body: R,
    val headers: Map<String, List<String>>
)
