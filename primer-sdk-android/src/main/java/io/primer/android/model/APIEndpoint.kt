package io.primer.android.model

import io.primer.android.model.dto.ClientSession

internal class APIEndpoint {

    enum class Target { CORE, PCI }

    companion object {

        const val PAYMENT_INSTRUMENTS = "/payment-instruments"

        const val CREATE_PAYPAL_BILLING_AGREEMENT = "/paypal/billing-agreements/create-agreement"
        const val CONFIRM_PAYPAL_BILLING_AGREEMENT = "/paypal/billing-agreements/confirm-agreement"
        const val CREATE_PAYPAL_ORDER = "/paypal/orders/create"

        const val CREATE_KLARNA_PAYMENT_SESSION = "/klarna/payment-sessions"
        const val FINALIZE_KLARNA_PAYMENT = "/klarna/payment-sessions/finalize"
        // const val SAVE_KLARNA_PAYMENT = "/klarna/customer-tokens"

        const val CREATE_GOCARDLESS_MANDATE = "/gocardless/mandates"

        const val DELETE_TOKEN = "/payment-instruments/{id}/vault"

        fun get(
            session: ClientSession,
            target: Target,
            pathname: String,
            params: Map<String, String>? = null,
        ): String {
            val baseUrl = if (target == Target.PCI) session.pciUrl else session.coreUrl
            var url = "$baseUrl$pathname"

            params?.entries?.forEach {
                url = url.replace("{${it.key}}", it.value)
            }

            return url
        }
    }
}
