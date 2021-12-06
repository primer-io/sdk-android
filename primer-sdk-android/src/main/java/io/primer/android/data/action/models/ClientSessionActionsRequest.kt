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
    }

    class SetPaymentMethod(
        val paymentMethodType: String,
        val network: String? = null,
    ) : Action(Type.SET_PAYMENT_METHOD)

    class UnsetPaymentMethod : Action(Type.UNSET_PAYMENT_METHOD)
}
