package io.primer.android

import kotlinx.serialization.Transient
import kotlinx.serialization.modules.SerializersModule

// this new way of declaring PaymentMethods is meant to improve modularisation as each method can
// be declared in a separate module (provided the marker interface is declared in shared one)
interface PaymentMethod {

    // this is here for backwards compatibility with old_PaymentMethod (to avoid a larger refactor)
    val identifier: String

    @Transient
    val module: PaymentMethodModule

    @Transient
    val serializersModule: SerializersModule
}
