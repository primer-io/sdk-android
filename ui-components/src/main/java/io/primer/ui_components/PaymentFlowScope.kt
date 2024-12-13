package io.primer.ui_components

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Stable
interface PaymentFlowScopeY {
    val globalState: GlobalState
    val clientSessionState: StateFlow<ClientSessionState>
    val paymentState: PaymentStateY
    val checkoutState: StateFlow<CheckoutState>


    // Utility Functions
    fun selectPaymentMethod(method: PrimerHeadlessUniversalCheckoutPaymentMethod)
    fun retryClientSessionUpdate()

    @Composable
    fun PaymentMethods(
        modifier: Modifier,
        parentLayout: @Composable (methods: List<PrimerHeadlessUniversalCheckoutPaymentMethod>, content: @Composable (PrimerHeadlessUniversalCheckoutPaymentMethod) -> Unit) -> Unit,
        paymentMethodContent: @Composable (method: PrimerHeadlessUniversalCheckoutPaymentMethod) -> Unit
    ) {
        if (paymentState.methods.isEmpty().not()) {
            val paymentMethods = listOf(
                PrimerHeadlessUniversalCheckoutPaymentMethod(
                    paymentMethodType = "PAYPAL",
                    paymentMethodName = "Paypal",
                    supportedPrimerSessionIntents = emptyList(),
                    paymentMethodManagerCategories = emptyList()
                ),
                PrimerHeadlessUniversalCheckoutPaymentMethod(
                    paymentMethodType = "KLARNA",
                    paymentMethodName = "Klarna",
                    supportedPrimerSessionIntents = emptyList(),
                    paymentMethodManagerCategories = emptyList()
                )
            )

            parentLayout(paymentMethods) { paymentMethod ->
                paymentMethodContent(paymentMethod)
            }
        }
    }

    @Composable
    fun DefaultPaymentMethod(
        modifier: Modifier,
        method: PrimerHeadlessUniversalCheckoutPaymentMethod,
        isSelected: Boolean,
        onClick: () -> Unit
    ) {
        PrimerPaymentMethodButtonComponent(modifier = modifier, paymentMethod = method) {
            onClick()
        }
    }

    sealed interface PaymentMethodScope {
        data object Initializing : PaymentMethodScope
        data object Rendered : PaymentMethodScope
    }

    data class ValidationState(val isValid: Boolean)


    @Composable
    fun DynamicPaymentMethodUI(
        modifier: Modifier,
        method: PrimerHeadlessUniversalCheckoutPaymentMethod,
        onStateChanged: @Composable (PaymentMethodScope, ValidationState) -> Unit
    )

    @Composable
    fun CheckoutResultScreen(status: CheckoutState)

    @Composable
    fun ClientSessionErrorScreen(onRetry: () -> Unit)

    // Lifecycle Callbacks
    fun onCheckoutComplete(action: (CheckoutState) -> Unit)
}

data class GlobalState(val isLoading: Boolean)

data class ClientSessionState(val isUpdating: Boolean)

data class PaymentStateY(
    val methods: List<PrimerHeadlessUniversalCheckoutPaymentMethod>,
    val selectedMethod: PrimerHeadlessUniversalCheckoutPaymentMethod? = null
)

data class CheckoutState(val isProcessing: Boolean)


class PaymentFlowViewModel(clientToken: String) : ViewModel() {
    val globalState = MutableStateFlow(GlobalState(isLoading = false))
    val clientSessionState = MutableStateFlow(ClientSessionState(isUpdating = false))
    val paymentState = MutableStateFlow(PaymentStateY(methods = emptyList()))
    val checkoutState = MutableStateFlow(CheckoutState(isProcessing = false))

    fun loadPaymentMethods() {
        globalState.value = globalState.value.copy(isLoading = true)
        viewModelScope.launch {
            // Simulate loading
            delay(2000)
            paymentState.value = PaymentStateY(
                methods = listOf(
                    PrimerHeadlessUniversalCheckoutPaymentMethod(
                        "PAYPAL",
                        "Paypal",
                        supportedPrimerSessionIntents = emptyList(),
                        paymentMethodManagerCategories = emptyList()
                    ),
                    PrimerHeadlessUniversalCheckoutPaymentMethod(
                        "KLARNA", "Klarna",
                        supportedPrimerSessionIntents = emptyList(),
                        paymentMethodManagerCategories = emptyList()
                    )
                )
            )
            globalState.value = globalState.value.copy(isLoading = false)
        }
    }

    fun updateClientSession() {
        clientSessionState.value = clientSessionState.value.copy(isUpdating = true)
        viewModelScope.launch {
            // Simulate session update
            delay(1500)
            clientSessionState.value = clientSessionState.value.copy(isUpdating = false)
        }
    }

    fun updateState(loading: Boolean) {
        globalState.update { globalState.value.copy(isLoading = loading) }
    }

    fun selectPaymentMethod(method: PrimerHeadlessUniversalCheckoutPaymentMethod) {
        paymentState.update { paymentState.value.copy(selectedMethod = method) }
    }

    fun processCheckout() {
        checkoutState.value = checkoutState.value.copy(isProcessing = true)
        viewModelScope.launch {
            // Simulate payment
            delay(3000)
            checkoutState.value = checkoutState.value.copy(isProcessing = false)
        }
    }
}

@Composable
fun PaymentFlowContainer(
    clientToken: String,
    onCheckoutComplete: (CheckoutState) -> Unit = {},
    content: @Composable PaymentFlowScopeY.() -> Unit
) {
    val viewModel = remember { PaymentFlowViewModel(clientToken) }

    val paymentState by viewModel.paymentState.collectAsStateWithLifecycle()
    val globalState by viewModel.globalState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = clientToken) {
        viewModel.loadPaymentMethods()
    }

    val scope = object : PaymentFlowScopeY {
        override val globalState: GlobalState = globalState
        override val clientSessionState: StateFlow<ClientSessionState>
            get() = viewModel.clientSessionState
        override val paymentState: PaymentStateY = paymentState
        override val checkoutState: StateFlow<CheckoutState>
            get() = viewModel.checkoutState

        override fun selectPaymentMethod(method: PrimerHeadlessUniversalCheckoutPaymentMethod) {
            viewModel.selectPaymentMethod(method)
        }

        override fun retryClientSessionUpdate() {
            //  viewModel.retryClientSessionUpdate()
        }

        @Composable
        override fun DynamicPaymentMethodUI(
            modifier: Modifier,
            method: PrimerHeadlessUniversalCheckoutPaymentMethod,
            onStateChanged: @Composable (PaymentFlowScopeY.PaymentMethodScope, PaymentFlowScopeY.ValidationState) -> Unit
        ) {
            if (paymentState.selectedMethod != null) {
              //  PrimerPaymentMethodDynamicComponent(modifier = modifier, viewModel, onStateChanged)
            }
        }


        @Composable
        override fun CheckoutResultScreen(status: CheckoutState) {
            Text("sgsgsgs")
        }

        @Composable
        override fun ClientSessionErrorScreen(onRetry: () -> Unit) {
            // DefaultClientSessionErrorScreen(onRetry)
        }

        override fun onCheckoutComplete(action: (CheckoutState) -> Unit) {
            // viewModel.setOnCheckoutComplete(action)
        }
    }

    content(scope)
}

