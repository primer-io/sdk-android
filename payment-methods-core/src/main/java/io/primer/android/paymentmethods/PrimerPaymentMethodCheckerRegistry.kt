package io.primer.android.paymentmethods

/**
 * Responsible for holding all the [PaymentMethodCheckerRegistry] for all the payment methods.
 * Each payment method should register its own [PaymentMethodChecker] with this class so its
 * availability can be evaluated when necessary. See [PaymentMethodChecker].
 */
interface PaymentMethodCheckerRegistry {
    val checkers: Map<String, PaymentMethodChecker>

    fun register(
        id: String,
        checker: PaymentMethodChecker,
    )

    fun unregister(id: String)

    operator fun get(id: String): PaymentMethodChecker? = checkers[id]
}

object PrimerPaymentMethodCheckerRegistry : PaymentMethodCheckerRegistry {
    private val _checkers: MutableMap<String, PaymentMethodChecker> =
        mutableMapOf()
    override val checkers: Map<String, PaymentMethodChecker>
        get() = _checkers

    override fun register(
        id: String,
        checker: PaymentMethodChecker,
    ) {
        _checkers[id] = checker
    }

    override fun unregister(id: String) {
        _checkers.remove(id)
    }
}
