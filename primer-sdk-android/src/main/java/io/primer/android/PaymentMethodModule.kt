package io.primer.android

import android.content.Context
import io.primer.android.model.dto.ClientSession
import io.primer.android.payment.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry

interface PaymentMethodModule {

    fun initialize(
        applicationContext: Context,
        clientSession: ClientSession
    )

    fun registerPaymentMethodCheckers(
        paymentMethodCheckerRegistry: PaymentMethodCheckerRegistry,
    )

    fun registerPaymentMethodDescriptorFactory(
        paymentMethodDescriptorFactoryRegistry: PaymentMethodDescriptorFactoryRegistry,
    )
}
