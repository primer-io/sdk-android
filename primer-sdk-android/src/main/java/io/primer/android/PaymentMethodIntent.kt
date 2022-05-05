package io.primer.android

enum class PaymentMethodIntent {
    CHECKOUT,
    VAULT;

    internal val isNotVault: Boolean
        get() = this != VAULT
    internal val isVault: Boolean
        get() = this == VAULT
    internal val isCheckout: Boolean
        get() = this == CHECKOUT
    internal val oppositeIntent: PaymentMethodIntent
        get() = when (this) {
            CHECKOUT -> VAULT
            VAULT -> CHECKOUT
        }
}
