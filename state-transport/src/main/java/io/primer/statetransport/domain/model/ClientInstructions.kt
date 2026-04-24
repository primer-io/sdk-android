package io.primer.statetransport.domain.model

sealed interface ClientInstructions {

    data class Execute(
        val pollDelayMilliseconds: Long,
        val payload: String,
    ) : ClientInstructions

    data class End(
        val checkoutOutcome: CheckoutOutcome?,
        val payment: PaymentInfo?,
    ) : ClientInstructions

    data class Wait(
        val pollDelayMilliseconds: Long,
    ) : ClientInstructions
}

enum class CheckoutOutcome {
    CHECKOUT_COMPLETE,
    CHECKOUT_FAILURE,
    DETERMINE_FROM_PAYMENT_STATUS,
}

data class PaymentInfo(
    val id: String,
    val date: String,
    val amount: Long,
    val currencyCode: String,
    val status: PaymentStatus,
    val customerId: String?,
    val orderId: String,
)

enum class PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED,
}
