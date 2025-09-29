package io.primer.android.internal.data.repositories

import android.content.Context
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.manager.nativeUi.PrimerHeadlessUniversalCheckoutNativeUiManager
import io.primer.android.components.manager.nativeUi.PrimerHeadlessUniversalCheckoutNativeUiManagerInterface
import io.primer.android.internal.domain.repositories.NativeUiRepository
import java.lang.ref.WeakReference

internal class NativeUiRepositoryImpl(
    private val context: WeakReference<Context>,
) : NativeUiRepository {

    private var nativeUiManager: PrimerHeadlessUniversalCheckoutNativeUiManagerInterface? = null

    override fun startPaymentFlow(paymentMethodType: String) {
        val ctx = context.get() ?: return

        cleanup()
        nativeUiManager = PrimerHeadlessUniversalCheckoutNativeUiManager.newInstance(
            paymentMethodType = paymentMethodType,
        ).also {
            it.showPaymentMethod(ctx, PrimerSessionIntent.CHECKOUT)
        }
    }

    override fun cleanup() {
        nativeUiManager?.cleanup()
        nativeUiManager = null
    }
}
