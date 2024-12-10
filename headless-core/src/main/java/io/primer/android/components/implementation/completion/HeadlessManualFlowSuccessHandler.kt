package io.primer.android.components.implementation.completion

import io.primer.android.payments.core.additionalInfo.PrimerCheckoutAdditionalInfo
import io.primer.android.payments.core.helpers.ManualFlowSuccessHandler

class HeadlessManualFlowSuccessHandler internal constructor() : ManualFlowSuccessHandler {
    override suspend fun handle(additionalInfo: PrimerCheckoutAdditionalInfo?) {
        // no-op
    }
}
