package io.primer.android.components.manager.nolPay

import io.primer.android.components.manager.core.composable.PrimerHeadlessStep

enum class NolPayLinkDataStep : PrimerHeadlessStep {

    COLLECT_PHONE_DATA,
    COLLECT_OTP_DATA,
    COLLECT_TAG_DATA,
    CARD_LINKED
}
