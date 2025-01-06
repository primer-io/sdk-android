package io.primer.android.ui.utils

import io.primer.android.paymentMethods.core.domain.repository.PrimerHeadlessRepository
import io.primer.android.payments.core.additionalInfo.PrimerCheckoutAdditionalInfo
import io.primer.android.payments.core.helpers.ManualFlowSuccessHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DropInManualFlowSuccessHandler internal constructor(
    private val primerHeadlessRepository: PrimerHeadlessRepository,
) : ManualFlowSuccessHandler {
    override suspend fun handle(additionalInfo: PrimerCheckoutAdditionalInfo?) {
        // ViewModels use Main.immediate dispatcher which causes race conditions with fragments that get popped
        // after this handler is called (StripeAchMandateFragment). Switch to the regular Main dispatcher to add some
        // delay.
        withContext(Dispatchers.Main) {
            primerHeadlessRepository.handleManualFlowSuccess(additionalInfo)
        }
    }
}
