package io.primer.android.paymentmethods.manager.composable

import io.primer.android.PrimerSessionIntent

interface PrimerHeadlessContextualStartable {
    fun start(
        paymentMethodType: String,
        sessionIntent: PrimerSessionIntent,
    )
}
