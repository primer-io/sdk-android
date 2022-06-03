package com.example.myapplication.constants

class PrimerRoutes {

    companion object {

        private const val root: String =
                "https://us-central1-primerdemo-8741b.cloudfunctions.net/api"

        const val clientToken: String = "$root/clientToken"

        const val clientSession: String = "$root/client-session"

        const val payments: String = "$root/payments"

        fun buildResumePaymentsUrl(id: String) = "$root/payments/${id}/resume"

        fun buildPaymentInstrumentsUrl(customerId: String) = "$root/payment-instruments?customer_id=${customerId}"
    }
}
