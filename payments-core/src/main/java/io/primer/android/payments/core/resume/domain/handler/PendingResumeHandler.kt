package io.primer.android.payments.core.resume.domain.handler

import io.primer.android.payments.core.additionalInfo.PrimerCheckoutAdditionalInfo

fun interface PendingResumeHandler {
    fun handle(additionalInfo: PrimerCheckoutAdditionalInfo)
}
