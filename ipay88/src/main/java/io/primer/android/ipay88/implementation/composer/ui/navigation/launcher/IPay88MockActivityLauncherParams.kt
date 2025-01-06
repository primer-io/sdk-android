package io.primer.android.ipay88.implementation.composer.ui.navigation.launcher

import io.primer.android.PrimerSessionIntent
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodRedirectLauncherParams

internal data class IPay88MockActivityLauncherParams(
    val errorCode: Int,
    override val sessionIntent: PrimerSessionIntent,
) : PaymentMethodRedirectLauncherParams(
        PaymentMethodType.IPAY88_CARD.name,
        sessionIntent,
    )
