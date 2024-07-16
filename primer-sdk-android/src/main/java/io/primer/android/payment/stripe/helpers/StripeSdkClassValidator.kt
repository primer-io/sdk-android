package io.primer.android.payment.stripe.helpers

internal object StripeSdkClassValidator {
    const val STRIPE_CLASS_NOT_LOADED_ERROR =
        "WARNING!\n" +
            "Stripe configuration has been found but dependency " +
            "'io.primer:stripe-android' is missing. " +
            "Add `io.primer:stripe-android' in your project so you can perform " +
            "payments with Stripe."
    private const val STRIPE_CLASS_NAME =
        "io.primer.android.stripe.StripeBankAccountCollectorActivity"

    fun isStripeSdkIncluded(): Boolean = try {
        Class.forName(STRIPE_CLASS_NAME)
        true
    } catch (ignored: ClassNotFoundException) {
        false
    }
}
