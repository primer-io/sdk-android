package io.primer.android.card.implementation.composer.ui.navigation.handler

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import io.primer.android.card.implementation.composer.ui.navigation.Card3DSNavigator
import io.primer.android.card.implementation.composer.ui.navigation.CardProcessor3DSNavigator
import io.primer.android.card.implementation.composer.ui.navigation.MockCard3DSNavigator
import io.primer.android.paymentmethods.core.ui.navigation.NavigationParams
import io.primer.paymentMethodCoreUi.core.ui.navigation.Navigator
import io.primer.paymentMethodCoreUi.core.ui.navigation.PaymentMethodContextNavigationHandler
import io.primer.paymentMethodCoreUi.core.ui.navigation.StartNewTaskNavigator

internal class CardNavigationHandler :
    PaymentMethodContextNavigationHandler {
    override fun getSupportedNavigators(context: Context): List<Navigator<NavigationParams>> {
        return listOf(StartNewTaskNavigator(context))
    }

    override fun getSupportedNavigators(
        activity: Activity,
        launcher: ActivityResultLauncher<Intent>
    ): List<Navigator<NavigationParams>> = listOf(
        Card3DSNavigator(activity, launcher),
        CardProcessor3DSNavigator(activity, launcher),
        MockCard3DSNavigator(activity, launcher)
    )
}
