package io.primer.composable.internal.domain.repositories

import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.domain.PrimerCheckoutData
import kotlinx.coroutines.flow.Flow

internal interface HeadlessRepository {

    suspend fun getAvailablePaymentMethods(): List<PrimerHeadlessUniversalCheckoutPaymentMethod>

    val paymentResults: Flow<Result<PrimerCheckoutData>>
}
