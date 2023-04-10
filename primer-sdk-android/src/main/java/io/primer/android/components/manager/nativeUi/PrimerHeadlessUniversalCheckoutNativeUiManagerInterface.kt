package io.primer.android.components.manager.nativeUi

import android.content.Context
import io.primer.android.PrimerSessionIntent
import io.primer.android.domain.exception.UnsupportedPaymentIntentException

@JvmDefaultWithCompatibility
interface PrimerHeadlessUniversalCheckoutNativeUiManagerInterface {

    /**
     * This method should be called when payment method needs to be showed.
     * @throws UnsupportedPaymentIntentException in case it is invoked for an unsupported [PrimerSessionIntent].
     */
    @Throws(UnsupportedPaymentIntentException::class)
    fun showPaymentMethod(context: Context, sessionIntent: PrimerSessionIntent)

    fun cleanup()
}
