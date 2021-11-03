package io.primer.android.viewmodel

import io.primer.android.model.dto.PaymentMethodType

/**
 * Responsible for holding all the [PaymentMethodCheckerRegistry] for all the payment methods.
 * Each payment method should register its own [PaymentMethodChecker] with this class so its
 * availability can be evaluated when necessary. See [PaymentMethodChecker].
 */
interface PaymentMethodCheckerRegistry {

    val checkers: Map<PaymentMethodType, PaymentMethodChecker>

    fun register(id: PaymentMethodType, checker: PaymentMethodChecker)
    fun unregister(id: PaymentMethodType)

    operator fun get(id: PaymentMethodType): PaymentMethodChecker? = checkers[id]
}

internal object PrimerPaymentMethodCheckerRegistry : PaymentMethodCheckerRegistry {

    private val _checkers: MutableMap<PaymentMethodType, PaymentMethodChecker> =
        mutableMapOf()
    override val checkers: Map<PaymentMethodType, PaymentMethodChecker>
        get() = _checkers

    override fun register(id: PaymentMethodType, checker: PaymentMethodChecker) {
        _checkers[id] = checker
    }

    override fun unregister(id: PaymentMethodType) {
        _checkers.remove(id)
    }
}
