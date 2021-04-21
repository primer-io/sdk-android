package io.primer.android.viewmodel

/**
 * Responsible for holding all the [PaymentMethodCheckerRegistrar] for all the payment methods.
 * Each payment method should register its own [PaymentMethodChecker] with this class so its
 * availability can be evaluated when necessary. See [PaymentMethodChecker].
 */
// TODO rename to PaymentMethodCheckerRegistry
internal interface PaymentMethodCheckerRegistrar {

    val checkers: Map<String, PaymentMethodChecker>

    fun register(id: String, checker: PaymentMethodChecker)
    fun unregister(id: String)

    operator fun get(id: String): PaymentMethodChecker? = checkers[id]
}

internal object PrimerPaymentMethodCheckerRegistrar : PaymentMethodCheckerRegistrar {

    private val _checkers: MutableMap<String, PaymentMethodChecker> = mutableMapOf()
    override val checkers: Map<String, PaymentMethodChecker> = _checkers

    override fun register(id: String, checker: PaymentMethodChecker) {
        _checkers[id] = checker
    }

    override fun unregister(id: String) {
        _checkers.remove(id)
    }
}
