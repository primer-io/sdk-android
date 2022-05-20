package io.primer.android

enum class PrimerPaymentMethodIntent {
    CHECKOUT,
    VAULT;

    internal val isNotVault: Boolean
        get() = this != VAULT
    internal val isVault: Boolean
        get() = this == VAULT
    internal val isCheckout: Boolean
        get() = this == CHECKOUT
    internal val oppositeIntent: PrimerPaymentMethodIntent
        get() = when (this) {
            CHECKOUT -> VAULT
            VAULT -> CHECKOUT
        }
}
