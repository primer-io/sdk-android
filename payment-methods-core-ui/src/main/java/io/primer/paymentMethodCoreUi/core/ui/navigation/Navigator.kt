package io.primer.paymentMethodCoreUi.core.ui.navigation

import io.primer.android.paymentmethods.core.ui.navigation.NavigationParams

interface Navigator<out T : NavigationParams> {

    fun navigate(params: @UnsafeVariance T)

    fun canHandle(params: NavigationParams): Boolean
}
