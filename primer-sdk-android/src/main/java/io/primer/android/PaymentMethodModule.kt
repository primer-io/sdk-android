package io.primer.android

import android.content.Context
import io.primer.android.data.configuration.models.ConfigurationData
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodDescriptorFactory
import io.primer.android.payment.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.viewmodel.PaymentMethodChecker
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry

/**
 * A **PaymentMethodModule** contains all the dependencies of a particular [PaymentMethod]. It can
 * be [initialized] if necessary and it can also register a [PaymentMethodChecker] with the
 * [PaymentMethodCheckerRegistry], as well as a [PaymentMethodDescriptorFactory] with the
 * [PaymentMethodDescriptorFactoryRegistry].
 */
internal interface PaymentMethodModule {

    /**
     * Initializes this payment method module, passing in all the information it may need to do
     * initialize itself.
     */
    fun initialize(
        applicationContext: Context,
        configuration: ConfigurationData,
    )

    /**
     * To be called when the SDK is at its startup phase. Each [PaymentMethod] can declare its
     * [PaymentMethodModule] specifying (among other things) its [PaymentMethodChecker], that will
     * be run at an appropriate time in order to determine if it (the [PaymentMethod]) should be
     * made available or not, at run-time.
     * @see [PaymentMethod]
     * @see [PaymentMethodDescriptor]
     * @see [PaymentMethodCheckerRegistry]
     */
    fun registerPaymentMethodCheckers(
        paymentMethodCheckerRegistry: PaymentMethodCheckerRegistry,
    )

    /**
     * To be called when the SDK is at its startup phase. Each [PaymentMethod] can declare its
     * [PaymentMethodModule] specifying (among other things) its [PaymentMethodDescriptorFactory].
     * This [PaymentMethodDescriptorFactory] will be used to create a [PaymentMethodDescriptor].
     * when needed.
     * @see [PaymentMethod]
     * @see [PaymentMethodDescriptor]
     * @see [PaymentMethodDescriptorFactoryRegistry]
     */
    fun registerPaymentMethodDescriptorFactory(
        paymentMethodDescriptorFactoryRegistry: PaymentMethodDescriptorFactoryRegistry,
    )
}
