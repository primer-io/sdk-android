package io.primer.android.payment

internal enum class SDKCapability {
    /**
     * A payment method which is available on Headless Checkout
     */
    HEADLESS,

    /**
     * A payment method which is available on Drop-In
     */
    DROP_IN,
}
