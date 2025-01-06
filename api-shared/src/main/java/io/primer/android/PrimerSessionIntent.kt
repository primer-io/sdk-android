package io.primer.android

enum class PrimerSessionIntent {
    CHECKOUT,
    VAULT,
    ;

    val isNotVault: Boolean
        get() = this != VAULT
    val isVault: Boolean
        get() = this == VAULT
    val isCheckout: Boolean
        get() = this == CHECKOUT
    val oppositeIntent: PrimerSessionIntent
        get() =
            when (this) {
                CHECKOUT -> VAULT
                VAULT -> CHECKOUT
            }
}
