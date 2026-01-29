package io.primer.android.internal.domain.repositories

import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.domain.PrimerCheckoutData

internal interface HeadlessRepository {

    suspend fun getAvailablePaymentMethods(): List<PrimerHeadlessUniversalCheckoutPaymentMethod>

    suspend fun awaitPaymentResult(): Result<PrimerCheckoutData>

    fun cleanup()
}
