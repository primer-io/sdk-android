package io.primer.components.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod

/**
 * A composable component that displays a a redirect based payment method.
 *
 * @param method The [payment method][PrimerHeadlessUniversalCheckoutPaymentMethod] associated with this component.
 * @param viewModel The associated [ViewModel] managing the payment flow.
 */
@Suppress("all")
@Composable
fun PrimerRedirectComponent(
    method: PrimerHeadlessUniversalCheckoutPaymentMethod,
    viewModel: ViewModel,
) { // TODO TWS: this should probably not take in a viewmodel
    Text("Continue to ${method.paymentMethodName.orEmpty()}")
}
