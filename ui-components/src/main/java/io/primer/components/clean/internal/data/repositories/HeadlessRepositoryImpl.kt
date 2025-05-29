package io.primer.components.clean.internal.data.repositories

import android.content.Context
import io.primer.android.components.PrimerHeadlessUniversalCheckoutInterface
import io.primer.android.components.PrimerHeadlessUniversalCheckoutListener
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.PrimerCheckoutData
import io.primer.components.clean.internal.domain.repositories.HeadlessRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal class HeadlessRepositoryImpl(
    private val headless: PrimerHeadlessUniversalCheckoutInterface
) : HeadlessRepository, DISdkComponent {

    override suspend fun getAvailablePaymentMethods() =
        suspendCancellableCoroutine { continuation ->
            headless.start(
                context = resolve<Context>(),
                clientToken = resolve<PrimerConfig>().clientTokenBase64!!,
                settings = resolve<PrimerConfig>().settings,
                checkoutListener = object : PrimerHeadlessUniversalCheckoutListener {
                    override fun onAvailablePaymentMethodsLoaded(paymentMethods: List<PrimerHeadlessUniversalCheckoutPaymentMethod>) =
                        continuation.resume(paymentMethods)

                    override fun onCheckoutCompleted(checkoutData: PrimerCheckoutData) = Unit
                }
            )
        }

}
