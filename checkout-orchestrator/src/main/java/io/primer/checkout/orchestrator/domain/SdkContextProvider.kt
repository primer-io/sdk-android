package io.primer.checkout.orchestrator.domain

fun interface SdkContextProvider {
    /** Without [paymentMethodType], the checkout's context, as client requirements are checked against. */
    fun provide(paymentMethodType: String?, paymentId: String?): String
}
