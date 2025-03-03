package io.primer.components.ui.components.native

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.components.Primer
import io.primer.components.models.PaymentTypeFactory

internal class NativeFactory : PaymentTypeFactory<Primer.Scope.PaymentMethod.GooglePay> {

    @Composable
    override fun createViewModel(): Primer.Scope.PaymentMethod.GooglePay {
        return viewModel<GooglePayViewModel>()
    }

    @Composable
    override fun render(scope: Primer.Scope.PaymentMethod.GooglePay) {
        PrimerNativeComponent(scope)
    }
}
