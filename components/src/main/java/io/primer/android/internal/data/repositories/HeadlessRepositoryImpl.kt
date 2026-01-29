package io.primer.android.internal.data.repositories

import android.content.Context
import io.primer.android.components.PrimerHeadlessUniversalCheckoutInterface
import io.primer.android.components.PrimerHeadlessUniversalCheckoutListener
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.internal.domain.error.PrimerErrorException
import io.primer.android.internal.domain.repositories.HeadlessRepository
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

internal class HeadlessRepositoryImpl(
    private val headless: PrimerHeadlessUniversalCheckoutInterface,
    private val context: WeakReference<Context>,
    private val primerConfig: PrimerConfig,
) : HeadlessRepository {

    override suspend fun awaitPaymentResult(): Result<PrimerCheckoutData> =
        suspendCancellableCoroutine { continuation ->
            val resumed = AtomicBoolean(false)
            headless.setCheckoutListener(object : PrimerHeadlessUniversalCheckoutListener {
                override fun onAvailablePaymentMethodsLoaded(
                    paymentMethods: List<PrimerHeadlessUniversalCheckoutPaymentMethod>,
                ) = Unit

                override fun onCheckoutCompleted(checkoutData: PrimerCheckoutData) {
                    if (resumed.compareAndSet(false, true)) {
                        continuation.resume(Result.success(checkoutData))
                    }
                }

                override fun onFailed(error: PrimerError, checkoutData: PrimerCheckoutData?) {
                    if (resumed.compareAndSet(false, true)) {
                        continuation.resume(Result.failure(PrimerErrorException(error, checkoutData)))
                    }
                }

                override fun onFailed(error: PrimerError) {
                    if (resumed.compareAndSet(false, true)) {
                        continuation.resume(Result.failure(PrimerErrorException(error)))
                    }
                }
            })
        }

    override suspend fun getAvailablePaymentMethods() =
        suspendCancellableCoroutine { continuation ->
            headless.start(
                context = context.get()!!,
                clientToken = primerConfig.clientTokenBase64!!,
                settings = primerConfig.settings,
                checkoutListener = object : PrimerHeadlessUniversalCheckoutListener {
                    override fun onAvailablePaymentMethodsLoaded(
                        paymentMethods: List<PrimerHeadlessUniversalCheckoutPaymentMethod>,
                    ) =
                        continuation.resume(
                            paymentMethods.filter { method ->
                                method.paymentMethodType == PaymentMethodType.PAYMENT_CARD.name ||
                                    supportedManagerCategories.any { category ->
                                        method.paymentMethodManagerCategories.contains(
                                            category,
                                        )
                                    }
                            },
                        )

                    override fun onCheckoutCompleted(checkoutData: PrimerCheckoutData) = Unit
                },
            )
        }

    override fun cleanup() = headless.cleanup(
        cleanClientSessionCache = primerConfig.settings.clientSessionCachingEnabled,
    )

    private companion object {

        val supportedManagerCategories =
            setOf(PrimerPaymentMethodManagerCategory.NATIVE_UI, PrimerPaymentMethodManagerCategory.KLARNA)
    }
}
