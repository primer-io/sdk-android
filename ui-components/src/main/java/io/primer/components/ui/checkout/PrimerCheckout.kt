package io.primer.components.ui.checkout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.manager.core.composable.PrimerValidationStatus
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.klarna.PrimerHeadlessUniversalCheckoutKlarnaManager
import io.primer.android.klarna.api.composable.KlarnaPaymentStep
import io.primer.components.domain.PaymentFlowScope
import io.primer.components.domain.PaymentMethodContentScope
import io.primer.components.domain.PaymentMethodState
import io.primer.components.domain.models.PaymentResult
import io.primer.components.domain.models.PaymentStatus
import io.primer.components.domain.models.PaymentValidationState
import io.primer.components.presentation.PrimerCheckoutViewModel
import io.primer.components.presentation.PrimerPaymentMethodViewModel
import io.primer.components.ui.PrimerDynamicComponent
import io.primer.components.ui.PrimerRedirectComponent
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@Composable
fun PrimerCheckout(
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
                val viewModelStoreOwner = LocalViewModelStoreOwner.current ?: error("...")
                val methodViewModel = remember(method.paymentMethodType) {
                    PrimerHeadlessUniversalCheckoutKlarnaManager(
                        viewModelStoreOwner = viewModelStoreOwner,
                    ).provideKlarnaComponent(PrimerSessionIntent.CHECKOUT)
                }

                val methodScope = remember(method, methodViewModel) {
                    object : PaymentMethodContentScope {
                        override val method = method
                        override val state = combine(
                            methodViewModel.componentStep,
                            methodViewModel.componentValidationStatus,
                        ) { klarnaPaymentStep, primerValidationStatus ->
                            PaymentMethodState.FormState(
                                isLoading = klarnaPaymentStep is KlarnaPaymentStep.PaymentSessionAuthorized,
                                validationState = PaymentValidationState(
                                    isValid = primerValidationStatus is PrimerValidationStatus.Valid,
                                ),
                            )
                        }.stateIn(
                            checkoutViewModel.viewModelScope,
                            SharingStarted.Eagerly,
                            PaymentMethodState.FormState(isLoading = true),
                        )

                        override fun submit(): Result<PaymentResult> {
                            return methodViewModel.submit().runSuspendCatching {
                                PaymentResult(status = PaymentStatus.COMPLETED)
                            }
                        }

                        @Composable
                        override fun DefaultContent() {
                            when (
                                method.paymentMethodManagerCategories
                                    .any { it == PrimerPaymentMethodManagerCategory.NATIVE_UI }
                            ) {
                                true -> PrimerRedirectComponent(method, methodViewModel)
                                false -> PrimerDynamicComponent(modifier = Modifier, methodViewModel)
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
        DefaultCheckoutContent(scope, onPaymentCompleted)
    }
}
