package com.example.myapplication.constants

object PrimerRoutes {

    private const val root: String = "https://us-central1-primerdemo-8741b.cloudfunctions.net"

    const val clientToken: String = "$root/clientToken"

    const val clientSession: String = "$root/clientSession"

    const val payments: String = "$root/payments"

    const val resumeToken: String = "$root/resume"
}