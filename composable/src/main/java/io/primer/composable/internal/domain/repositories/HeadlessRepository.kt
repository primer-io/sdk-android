package io.primer.composable.internal.domain.repositories

import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod

internal interface HeadlessRepository {

    suspend fun getAvailablePaymentMethods(): List<PrimerHeadlessUniversalCheckoutPaymentMethod>

}
