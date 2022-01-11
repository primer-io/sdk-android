package io.primer.android.data.action.models

data class ClientSessionActionsRequest(
    val actions: List<Action>
) {

    sealed class Action(
        val type: Type,
    )

    enum class Type {
        SET_PAYMENT_METHOD,
        UNSET_PAYMENT_METHOD,
        SET_BILLING_ADDRESS,
    }

    class SetPaymentMethod(
        val paymentMethodType: String,
        val network: String? = null,
    ) : Action(Type.SET_PAYMENT_METHOD)

    class UnsetPaymentMethod : Action(Type.UNSET_PAYMENT_METHOD)

    class SetBillingAddress(
        val firstName: String? = null,
        val lastName: String? = null,
        val addressLine1: String? = null,
        val addressLine2: String? = null,
        val city: String? = null,
        val postalCode: String? = null,
        val state: String? = null,
        val countryCode: String? = null,
    ) : Action(Type.SET_BILLING_ADDRESS)
}
