package io.primer.android.payment

enum class VaultCapability {
    /**
     * A payment method which can only be used for one-off payments
     */
    SINGLE_USE_ONLY,

    /**
     * A payment method which can only be used for recurring payments
     */
    VAULT_ONLY,

    /**
     * A payment method which can be used for both one-off and recurring payments
     */
    SINGLE_USE_AND_VAULT
}
