package io.primer.checkout.orchestrator.domain

fun interface ReturnUriProvider {
    fun provide(): String
}
