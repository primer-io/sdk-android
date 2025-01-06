package io.primer.android.paymentmethods.core.composer.provider

import io.primer.android.PrimerSessionIntent
import io.primer.android.core.di.DISdkComponent
import io.primer.android.paymentmethods.core.composer.PaymentMethodComposer

open class PaymentMethodComposerProvider {
    interface Factory : DISdkComponent {
        fun create(
            paymentMethodType: String,
            sessionIntent: PrimerSessionIntent,
        ): PaymentMethodComposer
    }
}
