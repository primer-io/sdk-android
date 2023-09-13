package io.primer.android.components.manager.nolPay

import io.primer.android.components.manager.core.composable.PrimerHeadlessStep

enum class NolPayUnlinkDataStep : PrimerHeadlessStep {

    COLLECT_CARD_DATA,
    COLLECT_PHONE_DATA,
    COLLECT_OTP_DATA,
    CARD_UNLINKED
}
