package io.primer.android.components.manager.nolPay

import io.primer.android.components.manager.core.composable.PrimerHeadlessStep

enum class NolPayStartPaymentStep : PrimerHeadlessStep {

    COLLECT_PHONE_DATA,
    COLLECT_CARD_DATA,
    COLLECT_TAG_DATA,
    PAYMENT_REQUESTED
}
