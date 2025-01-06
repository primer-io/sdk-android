package io.primer.paymentMethodCoreUi.core.ui.navigation.launchers

import io.primer.android.PrimerSessionIntent
import io.primer.android.paymentmethods.core.ui.navigation.NavigationParams
import java.io.Serializable

abstract class ActivityLauncherParams(
    open val paymentMethodType: String,
    open val sessionIntent: PrimerSessionIntent,
) : Serializable, NavigationParams
