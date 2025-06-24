package io.primer.android.internal.data.repositories

import android.content.Context
import io.primer.android.components.PrimerHeadlessUniversalCheckoutInterface
import io.primer.android.components.PrimerHeadlessUniversalCheckoutListener
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.internal.domain.repositories.HeadlessRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal class HeadlessRepositoryImpl(
    private val headless: PrimerHeadlessUniversalCheckoutInterface,
) : HeadlessRepository, DISdkComponent {

    override val paymentResults: Flow<Result<PrimerCheckoutData>> = callbackFlow {
        headless.setCheckoutListener(object : PrimerHeadlessUniversalCheckoutListener {
            override fun onAvailablePaymentMethodsLoaded(
                paymentMethods: List<PrimerHeadlessUniversalCheckoutPaymentMethod>,
            ) = Unit

            override fun onCheckoutCompleted(checkoutData: PrimerCheckoutData) {
                trySend(Result.success(checkoutData))
            }

            override fun onFailed(error: PrimerError, checkoutData: PrimerCheckoutData?) {
                trySend(Result.failure(Exception(error.description)))
            }

            override fun onFailed(error: PrimerError) {
                trySend(Result.failure(Exception(error.description)))
            }
        })

        awaitClose {
            // Clean up when flow is cancelled
        }
    }

    override suspend fun getAvailablePaymentMethods() =
        suspendCancellableCoroutine { continuation ->
            headless.start(
                context = resolve<Context>(),
                clientToken = resolve<PrimerConfig>().clientTokenBase64!!,
                settings = resolve<PrimerConfig>().settings,
                checkoutListener = object : PrimerHeadlessUniversalCheckoutListener {
                    override fun onAvailablePaymentMethodsLoaded(
                        paymentMethods: List<PrimerHeadlessUniversalCheckoutPaymentMethod>,
                    ) =
                        continuation.resume(paymentMethods)

                    override fun onCheckoutCompleted(checkoutData: PrimerCheckoutData) = Unit
                },
            )
        }
}
