package io.primer.android.internal.presentation.screens.nativeUi

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.android.core.di.DISdkContext
import io.primer.android.internal.presentation.checkout.components.DefaultLoadingScreen

@Composable
internal fun NativeUiPaymentMethodScreen(paymentMethod: String) {
    with(requireNotNull(DISdkContext.componentsSdkContainer)) {
        viewModel<NativeUiPaymentMethodViewModel>(
            factory = NativeUiPaymentMethodViewModelFactory(
                paymentMethodType = paymentMethod,
                checkoutNavigator = resolve(),
                startNativeUiPaymentUseCase = resolve(),
            ),
        )
    }

    DefaultLoadingScreen()
}
