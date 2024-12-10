package io.primer.android.components.manager.nativeUi

import android.content.Context
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.DefaultNativeUiManagerHeadlessManagerDelegate
import io.primer.android.components.SdkUninitializedException
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.inject
import io.primer.android.domain.exception.UnsupportedPaymentMethodException

class PrimerHeadlessUniversalCheckoutNativeUiManager private constructor(
    private val paymentMethodType: String
) : PrimerHeadlessUniversalCheckoutNativeUiManagerInterface, DISdkComponent {

    private val delegate: DefaultNativeUiManagerHeadlessManagerDelegate by inject()

    init {
        delegate.init(paymentMethodType, PrimerPaymentMethodManagerCategory.NATIVE_UI)
    }

    override fun showPaymentMethod(
        context: Context,
        sessionIntent: PrimerSessionIntent
    ) {
        delegate.dispatchAction(paymentMethodType) { error ->
            if (error == null) {
                delegate.start(
                    context = context,
                    paymentMethodType = paymentMethodType,
                    sessionIntent = sessionIntent,
                    category = PrimerPaymentMethodManagerCategory.NATIVE_UI,
                    onPostStart = {}
                )
            }
        }
    }

    override fun cleanup() {
        delegate.cleanup()
    }

    companion object {
        /**
         * Creates Native UI manager tied to current session for a given payment method.
         *
         * @param paymentMethodType the payment method flow to be shown.
         * @throws SdkUninitializedException
         * @throws UnsupportedPaymentMethodException
         */

        @Throws(SdkUninitializedException::class, UnsupportedPaymentMethodException::class)
        @JvmStatic
        fun newInstance(
            paymentMethodType: String
        ): PrimerHeadlessUniversalCheckoutNativeUiManagerInterface = PrimerHeadlessUniversalCheckoutNativeUiManager(
            paymentMethodType = paymentMethodType
        )
    }
}
