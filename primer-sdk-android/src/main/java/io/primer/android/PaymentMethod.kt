package io.primer.android

import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodDescriptorFactory
import io.primer.android.viewmodel.PaymentMethodChecker
import kotlinx.serialization.Transient
import kotlinx.serialization.modules.SerializersModule

/**
 * Represents an abstract **PaymentMethod**. A PaymentMethod declares a [PaymentMethodModule] and a
 * [SerializersModule].
 * A [PaymentMethodModule] is used to:
 * 1. Initialize any internal dependencies it may have (see [PaymentMethodModule.initialize]);
 * 1. Declare its [PaymentMethodChecker] it may require, which will determine if it (the
 * PaymentMethod) will be available at run-time, or not;
 * 1. Declare its [PaymentMethodDescriptorFactory] that will be used to create the corresponding
 * [PaymentMethodDescriptor].
 *
 * The [SerializersModule] is necessary so that Kotlin's serialization is properly setup, in order
 * for all payment methods to be serialized and passed from the [UniversalCheckout] to the
 * [CheckoutSheetActivity]. Note that this is currently necessary but ideally we'll be removing it
 * (e.g. if we stop requiring the host app to declare all payment methods it wants to support
 * via the [UniversalCheckout]).
 * @see [PaymentMethodModule]
 * @see [SerializersModule]
 */
interface PaymentMethod {

    // FIXME this is here for backwards compatibility as we're still relying on identifiers
    //  ideally all payment methods would be parsed into the respective classes (that implement
    //  this interface) at runtime
    val identifier: String

    val canBeVaulted: Boolean

    @Transient
    val module: PaymentMethodModule

    @Transient
    val serializersModule: SerializersModule
}
