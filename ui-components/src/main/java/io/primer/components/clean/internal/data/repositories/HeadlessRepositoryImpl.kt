package io.primer.components.clean.internal.data.repositories

import io.primer.android.components.PrimerHeadlessUniversalCheckoutInterface
import io.primer.android.components.PrimerHeadlessUniversalCheckoutListener
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.core.di.DISdkContext
import io.primer.android.domain.PrimerCheckoutData
import io.primer.components.clean.internal.domain.repositories.HeadlessRepository

internal class HeadlessRepositoryImpl(
    private val headless: PrimerHeadlessUniversalCheckoutInterface
) : HeadlessRepository, PrimerHeadlessUniversalCheckoutListener {

    override suspend fun getAvailablePaymentMethods() {
        headless.start(
            context = DISdkContext.container().resolve(),
            clientToken = DISdkContext.container().resolve(),
            settings = DISdkContext.container().resolve()
        )
    }

    override fun onAvailablePaymentMethodsLoaded(paymentMethods: List<PrimerHeadlessUniversalCheckoutPaymentMethod>) {
        TODO("return back with flow")
    }

    override fun onCheckoutCompleted(checkoutData: PrimerCheckoutData) {
        TODO("return back with flow")
    }

}
