package io.primer.android.internal.domain.repositories

import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.internal.domain.Cleanable

internal interface HeadlessRepository : Cleanable {

    suspend fun getAvailablePaymentMethods(): List<PrimerHeadlessUniversalCheckoutPaymentMethod>

    suspend fun awaitPaymentResult(): Result<PrimerCheckoutData>
}
