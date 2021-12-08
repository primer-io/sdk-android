package com.example.myapplication.constants

class PrimerRoutes {


    companion object {

        private const val root: String =
            "https://us-central1-primerdemo-8741b.cloudfunctions.net/api"

        const val clientToken: String = "$root/clientToken"

        const val clientSession: String = "$root/client-session"

        const val payments: String = "$root/payments"

        const val actions: String = "$root/client-session/actions"

        fun buildResumePaymentsUrl(id: String) = "$root/payments/${id}/resume"
    }
}