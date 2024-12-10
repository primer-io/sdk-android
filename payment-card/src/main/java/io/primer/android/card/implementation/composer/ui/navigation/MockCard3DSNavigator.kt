package io.primer.android.card.implementation.composer.ui.navigation

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import io.primer.android.PrimerSessionIntent
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.core.ui.navigation.NavigationParams
import io.primer.paymentMethodCoreUi.core.ui.mock.PaymentMethodMockActivity
import io.primer.paymentMethodCoreUi.core.ui.navigation.StartActivityForResultNavigator
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodRedirectLauncherParams

internal data class MockCard3DSActivityLauncherParams(override val paymentMethodType: String) :
    PaymentMethodRedirectLauncherParams(
        paymentMethodType = paymentMethodType,
        sessionIntent = PrimerSessionIntent.CHECKOUT
    )

internal data class MockCard3DSNavigator(
    private val context: Activity,
    override val launcher: ActivityResultLauncher<Intent>
) : StartActivityForResultNavigator<MockCard3DSActivityLauncherParams>(launcher) {

    override fun navigate(params: MockCard3DSActivityLauncherParams) {
        launcher.launch(
            PaymentMethodMockActivity.getLaunchIntent(
                context = context,
                paymentMethodType = PaymentMethodType.PAYMENT_CARD.name
            )
        )
    }

    override fun canHandle(params: NavigationParams): Boolean {
        return params is MockCard3DSActivityLauncherParams
    }
}
