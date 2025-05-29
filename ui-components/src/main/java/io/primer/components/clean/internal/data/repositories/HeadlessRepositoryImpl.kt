package io.primer.components.clean.internal.data.repositories

import io.primer.android.components.PrimerHeadlessUniversalCheckoutInterface
import io.primer.android.components.PrimerHeadlessUniversalCheckoutListener
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.core.di.DISdkContext
import io.primer.android.domain.PrimerCheckoutData
import io.primer.components.clean.internal.domain.repositories.HeadlessRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

internal class HeadlessRepositoryImpl(
    private val headless: PrimerHeadlessUniversalCheckoutInterface
) : HeadlessRepository, PrimerHeadlessUniversalCheckoutListener {

    private var paymentMethodsContinuation: Continuation<List<PrimerHeadlessUniversalCheckoutPaymentMethod>>? = null

    override suspend fun getAvailablePaymentMethods(): List<PrimerHeadlessUniversalCheckoutPaymentMethod> {
        return suspendCancellableCoroutine { continuation ->
            paymentMethodsContinuation = continuation

            continuation.invokeOnCancellation {
                paymentMethodsContinuation = null
            }

            headless.start(
                context = DISdkContext.container().resolve(),
                clientToken = DISdkContext.container().resolve(),
                settings = DISdkContext.container().resolve()
            )
        }
    }

    override fun onAvailablePaymentMethodsLoaded(paymentMethods: List<PrimerHeadlessUniversalCheckoutPaymentMethod>) {
        paymentMethodsContinuation?.resume(paymentMethods)
        paymentMethodsContinuation = null
    }

    override fun onCheckoutCompleted(checkoutData: PrimerCheckoutData) {
        TODO("return back with flow")
    }

}
