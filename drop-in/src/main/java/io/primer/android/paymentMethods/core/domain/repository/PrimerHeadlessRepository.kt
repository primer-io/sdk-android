package io.primer.android.paymentMethods.core.domain.repository

import io.primer.android.paymentMethods.core.domain.events.PrimerEvent
import io.primer.android.payments.core.additionalInfo.PrimerCheckoutAdditionalInfo
import kotlinx.coroutines.flow.Flow

internal interface PrimerHeadlessRepository {

    val events: Flow<PrimerEvent>

    fun start(clientToken: String)

    suspend fun handleManualFlowSuccess(additionalInfo: PrimerCheckoutAdditionalInfo?)
}
