package io.primer.sample.constants

object PrimerRoutes {
    private const val ROOT = "https://us-central1-primerdemo-8741b.cloudfunctions.net/api"

    val clientSession: String get() = "$ROOT/client-session"

    val payments: String get() = "$ROOT/payments"

    fun buildResumePaymentsUrl(id: String) = "$ROOT/payments/${id}/resume"
}
