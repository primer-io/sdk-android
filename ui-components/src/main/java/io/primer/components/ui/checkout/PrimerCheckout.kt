package io.primer.components.ui.checkout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.components.domain.PaymentFlowScope
import io.primer.components.domain.PaymentMethodContentScope
import io.primer.components.domain.PaymentMethodState
import io.primer.components.domain.models.PaymentResult
import io.primer.components.domain.models.PaymentStatus
import io.primer.components.presentation.PrimerCheckoutViewModel
import io.primer.components.presentation.PrimerPaymentMethodViewModel
import io.primer.components.ui.PrimerCardFormComponent
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * A composable function that serves as the entry point for initiating and managing payments using the Primer SDK.
 *
 * @param modifier The [Modifier] to be applied to the layout.
 * @param clientToken The client token required for authorization.
 * @param onPaymentCompleted A callback invoked upon successful completion of a payment.
 * @param content An optional composable lambda that allows building a completely custom checkout UI using data provided
 * by a [PaymentFlowScope]. If omitted, the default implementation displays payment methods in a vertically scrollable
 * list.
 */
@Composable
fun PrimerCheckout(
    modifier: Modifier = Modifier,
    clientToken: String,
    onPaymentCompleted: (PaymentResult) -> Unit = {},
    content: (@Composable PaymentFlowScope.() -> Unit)? = null,
) {
    val checkoutViewModel = remember {
        PrimerCheckoutViewModel(clientToken)
    }

    val paymentMethodViewModel = remember {
        PrimerPaymentMethodViewModel()
    }

    val scope = remember(checkoutViewModel) {
        object : PaymentFlowScope {
            override val paymentMethods = checkoutViewModel.paymentMethods
            override val selectedMethod = paymentMethodViewModel.selectedMethod

            override fun selectPaymentMethod(method: PrimerHeadlessUniversalCheckoutPaymentMethod?) {
                paymentMethodViewModel.selectMethod(method)
            }

            @Composable
            override fun PaymentMethodContent(
                method: PrimerHeadlessUniversalCheckoutPaymentMethod,
                content: @Composable PaymentMethodContentScope.() -> Unit,
            ) {

                val methodScope = remember(method) {
                    object : PaymentMethodContentScope {
                        override val method = method
                        override val state = MutableStateFlow(PaymentMethodState.FormState(isLoading = true))
                        override fun submit() = Result.success(PaymentResult(PaymentStatus.COMPLETED))

                        @Composable
                        override fun DefaultContent() {
                            PrimerCardFormComponent {

                            }
                        }
                    }
                }
                content(methodScope)
            }
        }
    }

    if (content != null) {
        content(scope)
    } else {
        DefaultCheckoutContent(modifier = modifier, scope = scope, onPaymentCompleted = onPaymentCompleted)
    }
}
