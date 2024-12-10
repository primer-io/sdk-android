package io.primer.android.paymentmethods

/**
 * Represents an abstract **PaymentMethod**. A PaymentMethod declares a [PaymentMethodModule].
 * A [PaymentMethodModule] is used to:
 * 1. Initialize any internal dependencies it may have (see [PaymentMethodModule.initialize]);
 * 1. Declare its [PaymentMethodChecker] it may require, which will determine if it (the
 * PaymentMethod) will be available at run-time, or not;
 * 1. Declare its [PaymentMethodDescriptorFactory] that will be used to create the corresponding
 * [PaymentMethodDescriptor].
 * @see [PaymentMethodModule]
 */
interface PaymentMethod {

    val type: String

    val canBeVaulted: Boolean

    val module: PaymentMethodModule
}
