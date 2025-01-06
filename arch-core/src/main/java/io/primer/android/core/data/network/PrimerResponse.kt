package io.primer.android.core.data.network

data class PrimerResponse<R>(
    val body: R,
    val headers: Map<String, List<String>>,
)
