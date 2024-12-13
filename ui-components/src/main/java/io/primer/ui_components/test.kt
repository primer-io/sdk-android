package io.primer.ui_components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.manager.core.composable.PrimerValidationStatus
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.klarna.PrimerHeadlessUniversalCheckoutKlarnaManager
import io.primer.android.klarna.api.component.KlarnaComponent
import io.primer.android.klarna.api.composable.KlarnaPaymentStep
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PaymentValidationState(
    val isValid: Boolean = false,
    val errors: List<ValidationError> = emptyList()
)

data class ValidationError(
    val code: String,
    val message: String
)

data class PaymentResult(
    val status: PaymentStatus,
    val transactionId: String? = null,
    val error: String? = null
)

enum class PaymentStatus {
    COMPLETED,
    FAILED,
    CANCELLED
}

// States
sealed interface PaymentMethodState {
    val validationState: PaymentValidationState
    val isLoading: Boolean

    data class RedirectState(
        override val validationState: PaymentValidationState = PaymentValidationState(isValid = true),
        override val isLoading: Boolean = false,
        val redirectUrl: String? = null
    ) : PaymentMethodState

    data class FormState(
        override val validationState: PaymentValidationState = PaymentValidationState(),
        override val isLoading: Boolean = false,
    ) : PaymentMethodState
}


// Core scope interfaces
interface PaymentFlowScopeZ {
    val paymentMethods: StateFlow<List<PrimerHeadlessUniversalCheckoutPaymentMethod>>
    val selectedMethod: StateFlow<PrimerHeadlessUniversalCheckoutPaymentMethod?>

    fun selectPaymentMethod(method: PrimerHeadlessUniversalCheckoutPaymentMethod?)
    fun getMethodState(method: PrimerHeadlessUniversalCheckoutPaymentMethod): StateFlow<PaymentMethodState>

    @Composable
    fun PaymentMethodContent(
        method: PrimerHeadlessUniversalCheckoutPaymentMethod,
        content: @Composable PaymentMethodContentScope.() -> Unit
    )
}

interface PaymentMethodContentScope {
    val method: PrimerHeadlessUniversalCheckoutPaymentMethod
    val state: StateFlow<PaymentMethodState>

    suspend fun submit(): Result<PaymentResult>

    @Composable
    fun DefaultContent()
}

// ViewModel
class PaymentFlowViewModelZ(
    private val clientToken: String
) : ViewModel() {
    private val _paymentMethods = MutableStateFlow<List<PrimerHeadlessUniversalCheckoutPaymentMethod>>(emptyList())
    val paymentMethods = _paymentMethods.asStateFlow()

    private val _selectedMethod = MutableStateFlow<PrimerHeadlessUniversalCheckoutPaymentMethod?>(null)
    val selectedMethod = _selectedMethod.asStateFlow()

    private val _methodStates = MutableStateFlow<Map<String, PaymentMethodState>>(emptyMap())
    val methodStates = _methodStates.asStateFlow()

    init {
        initialize()
    }

    private fun initialize() {
        viewModelScope.launch {
            try {
                loadPaymentMethods()
            } catch (e: Exception) {
                // Handle initialization error
            }
        }
    }

    private suspend fun loadPaymentMethods() {
        try {
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
            _paymentMethods.value = paymentMethods

            // Initialize states
            _methodStates.value = paymentMethods.associate { method ->
                method.paymentMethodType to when (method.paymentMethodManagerCategories.any { it == PrimerPaymentMethodManagerCategory.NATIVE_UI }) {
                    true -> PaymentMethodState.RedirectState()
                    false -> PaymentMethodState.FormState()
                }
            }
        } catch (e: Exception) {
            // Handle loading error
        }
    }

    fun selectMethod(method: PrimerHeadlessUniversalCheckoutPaymentMethod?) {
        _selectedMethod.value = method
    }

    fun getMethodState(methodId: String): StateFlow<PaymentMethodState> {
        return methodStates.map { it[methodId] ?: PaymentMethodState.FormState() }
            .stateIn(viewModelScope, SharingStarted.Lazily, PaymentMethodState.FormState())
    }
}

// Main composable
@Composable
fun PrimerCheckout(
    clientToken: String,
    onPaymentCompleted: (PaymentResult) -> Unit = {},
    content: (@Composable PaymentFlowScopeZ.() -> Unit)? = null
) {
    val viewModel = remember {
        PaymentFlowViewModelZ(clientToken)
    }

    val scope = remember(viewModel) {
        object : PaymentFlowScopeZ {
            override val paymentMethods = viewModel.paymentMethods
            override val selectedMethod = viewModel.selectedMethod

            override fun selectPaymentMethod(method: PrimerHeadlessUniversalCheckoutPaymentMethod?) {
                viewModel.selectMethod(method)
            }

            override fun getMethodState(method: PrimerHeadlessUniversalCheckoutPaymentMethod) =
                viewModel.getMethodState(method.paymentMethodType)

            @Composable
            override fun PaymentMethodContent(
                method: PrimerHeadlessUniversalCheckoutPaymentMethod,
                content: @Composable PaymentMethodContentScope.() -> Unit
            ) {
                val viewModelStoreOwner = LocalViewModelStoreOwner.current ?: error("...")
                val methodViewModel = remember(method.paymentMethodType) {
                    PrimerHeadlessUniversalCheckoutKlarnaManager(
                        viewModelStoreOwner = viewModelStoreOwner
                    ).provideKlarnaComponent(PrimerSessionIntent.CHECKOUT)
                }

                val methodScope = remember(method, methodViewModel) {
                    object : PaymentMethodContentScope {
                        override val method = method
                        override val state = combine(
                            methodViewModel.componentStep,
                            methodViewModel.componentValidationStatus
                        ) { klarnaPaymentStep, primerValidationStatus ->
                            PaymentMethodState.FormState(
                                isLoading = klarnaPaymentStep is KlarnaPaymentStep.PaymentSessionAuthorized,
                                validationState = PaymentValidationState(
                                    isValid = primerValidationStatus is PrimerValidationStatus.Valid
                                )
                            )
                        }.stateIn(
                            viewModel.viewModelScope,
                            SharingStarted.Lazily,
                            PaymentMethodState.FormState(isLoading = true)
                        )

                        override suspend fun submit(): Result<PaymentResult> {
                            return methodViewModel.submit().runSuspendCatching {
                                PaymentResult(status = PaymentStatus.COMPLETED)
                            }
                        }

                        @Composable
                        override fun DefaultContent() {
                            when (method.paymentMethodManagerCategories.any { it == PrimerPaymentMethodManagerCategory.NATIVE_UI }) {
                                true -> RedirectContent(method, methodViewModel)
                                false -> FormContent(method, methodViewModel)
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

// Default implementation
@Composable
private fun DefaultCheckoutContent(
    scope: PaymentFlowScopeZ,
    onPaymentCompleted: (PaymentResult) -> Unit
) {
    val selectedMethod by scope.selectedMethod.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    Column(modifier = Modifier.fillMaxWidth()) {
        when (selectedMethod) {
            null -> {
                val methods by scope.paymentMethods.collectAsState()
                LazyColumn {
                    items(methods) { method ->
                        ListItem(
                            headlineContent = { Text(method.paymentMethodName.orEmpty()) },
                            modifier = Modifier.clickable {
                                scope.selectPaymentMethod(method)
                            }
                        )
                    }
                }
            }

            else -> {
                scope.PaymentMethodContent(selectedMethod!!) {
                    Column {
                        IconButton(onClick = { scope.selectPaymentMethod(null) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                        }

                        DefaultContent()

                        val methodState by state.collectAsState()
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    submit().onSuccess { result ->
                                        onPaymentCompleted(result)
                                    }
                                }
                            },
                            enabled = methodState.validationState.isValid
                        ) {
                            Text("Pay with ${method.paymentMethodName.orEmpty()}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RedirectContent(method: PrimerHeadlessUniversalCheckoutPaymentMethod, viewModel: ViewModel) {
    Text("Continue to ${method.paymentMethodName.orEmpty()}")
}

@Composable
private fun FormContent(method: PrimerHeadlessUniversalCheckoutPaymentMethod, viewModel: KlarnaComponent) {
    PrimerPaymentMethodDynamicComponent(modifier = Modifier, viewModel)
}