package io.primer.android.payment

import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.manager.nativeUi.PrimerHeadlessUniversalCheckoutNativeUiManager
import io.primer.android.components.manager.nativeUi.PrimerHeadlessUniversalCheckoutNativeUiManagerInterface
import io.primer.android.paymentMethods.PaymentMethodBehaviour

internal class NativeUiSelectedPaymentMethodManagerBehaviour(
    @get:VisibleForTesting val paymentMethodType: String,
    @get:VisibleForTesting val sessionIntent: PrimerSessionIntent
) : PaymentMethodBehaviour {

    fun execute(context: AppCompatActivity): PrimerHeadlessUniversalCheckoutNativeUiManagerInterface =
        PrimerHeadlessUniversalCheckoutNativeUiManager.newInstance(paymentMethodType)
            .also { manager -> manager.showPaymentMethod(context = context, sessionIntent = sessionIntent) }
}
