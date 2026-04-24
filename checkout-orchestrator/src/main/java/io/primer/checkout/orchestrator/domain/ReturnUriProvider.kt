package io.primer.checkout.orchestrator.domain

fun interface ReturnUriProvider {
    suspend fun provide(): String
}
