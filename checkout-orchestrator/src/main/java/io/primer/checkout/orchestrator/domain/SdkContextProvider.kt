package io.primer.checkout.orchestrator.domain

fun interface SdkContextProvider {
    fun provide(paymentMethodType: String): String
}
