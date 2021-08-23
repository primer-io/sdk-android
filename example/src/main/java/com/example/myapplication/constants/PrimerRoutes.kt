package com.example.myapplication.constants

object PrimerRoutes {

    private const val root: String = "https://us-central1-primerdemo-8741b.cloudfunctions.net"

    const val clientTokenUri: String = "$root/clientToken"

    const val transactionUri: String = "$root/transaction"

    const val paymentUri: String = "$root/payments"

}