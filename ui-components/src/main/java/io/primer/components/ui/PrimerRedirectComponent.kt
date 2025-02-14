package io.primer.components.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod

@Suppress("all")
@Composable
fun PrimerRedirectComponent(method: PrimerHeadlessUniversalCheckoutPaymentMethod, viewModel: ViewModel) {
    Text("Continue to ${method.paymentMethodName.orEmpty()}")
}
