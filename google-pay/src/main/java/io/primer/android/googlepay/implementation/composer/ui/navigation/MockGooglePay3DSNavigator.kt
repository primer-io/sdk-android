package io.primer.android.googlepay.implementation.composer.ui.navigation

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import io.primer.android.PrimerSessionIntent
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.core.ui.navigation.NavigationParams
import io.primer.paymentMethodCoreUi.core.ui.mock.PaymentMethodMockActivity
import io.primer.paymentMethodCoreUi.core.ui.navigation.StartActivityForResultNavigator
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodRedirectLauncherParams

internal data class MockGooglePay3DSActivityLauncherParams(override val paymentMethodType: String) :
    PaymentMethodRedirectLauncherParams(
        paymentMethodType = paymentMethodType,
        sessionIntent = PrimerSessionIntent.CHECKOUT
    )

internal data class MockGooglePay3DSNavigator(
    private val context: Activity,
    override val launcher: ActivityResultLauncher<Intent>
) : StartActivityForResultNavigator<MockGooglePay3DSActivityLauncherParams>(launcher) {

    override fun navigate(params: MockGooglePay3DSActivityLauncherParams) {
        launcher.launch(
            PaymentMethodMockActivity.getLaunchIntent(
                context = context,
                paymentMethodType = PaymentMethodType.PAYMENT_CARD.name
            )
        )
    }

    override fun canHandle(params: NavigationParams): Boolean {
        return params is MockGooglePay3DSActivityLauncherParams
    }
}
