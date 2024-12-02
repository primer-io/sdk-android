package io.primer.ui_components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod


sealed interface PaymentState {
    data class PaymentMethodList1(val methods: List<PrimerHeadlessUniversalCheckoutPaymentMethod>) : PaymentState
    data object Loading : PaymentState
}

sealed interface PaymentEvent {
    data class PaymentMethodSelected(val method: PrimerHeadlessUniversalCheckoutPaymentMethod) : PaymentEvent
}

interface PaymentMethodListScope {

    val paymentMethods: List<PrimerHeadlessUniversalCheckoutPaymentMethod>

    @Composable
    fun PaymentMethod(
        method: PrimerHeadlessUniversalCheckoutPaymentMethod,
        content: @Composable PaymentMethodListScope.() -> Unit
    )
}

class PaymentMethodListScopeImpl(
    private val methods: List<PrimerHeadlessUniversalCheckoutPaymentMethod>,
    private val onEvent: (PaymentEvent) -> Unit,
    private val onPaymentMethodSelected: (PrimerHeadlessUniversalCheckoutPaymentMethod) -> Unit = {
        println("selected")
    }
) : PaymentMethodListScope {

    override val paymentMethods: List<PrimerHeadlessUniversalCheckoutPaymentMethod>
        get() = methods

    @Composable
    override fun PaymentMethod(
        method: PrimerHeadlessUniversalCheckoutPaymentMethod,
        content: @Composable PaymentMethodListScope.() -> Unit
    ) {
        Button(onClick = { onEvent(PaymentEvent.PaymentMethodSelected(method)) }) {
            Text(text = method.paymentMethodName.orEmpty())
        }
    }
}


internal class PaymentFlowScopeImpl(
    private val state: PaymentState,
    private val onEvent: (PaymentEvent) -> Unit,
    private val content: @Composable PaymentFlowScope.() -> Unit
) :
    PaymentFlowScope {

    @Composable
    override fun PaymentMethodList(
        onPaymentMethodSelected: (PrimerHeadlessUniversalCheckoutPaymentMethod) -> Unit,
        content: @Composable PaymentMethodListScope.() -> Unit
    ) {
        if (state is PaymentState.PaymentMethodList1) {
            PaymentMethodListScopeImpl(
                methods = state.methods,
                onEvent = onEvent,
                onPaymentMethodSelected
            ).apply { content() }
        }
    }

    @Composable
    override fun Loading(content: @Composable () -> Unit) {
        if (state is PaymentState.Loading) {
            CircularProgressIndicator()
        }
    }

    @Composable
    fun Render() {
        content() // Render the `content` block passed to PaymentFlowScopeImpl
    }

}

interface PaymentFlowScope {

    @Composable
    fun PaymentMethodList(
        onPaymentMethodSelected: (PrimerHeadlessUniversalCheckoutPaymentMethod) -> Unit,
        content: @Composable PaymentMethodListScope.() -> Unit
    )

    //
//    @Composable
//    fun PaymentMethodUI(content: PaymentMethodUIScope.() -> Unit)
//
//
    @Composable
    fun Loading(content: @Composable () -> Unit)

//    @Composable
//    fun Error(content: @Composable (String) -> Unit)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentBottomSheetFlow(
    onEvent: (PaymentEvent) -> Unit,
    content: @Composable PaymentFlowScope.() -> Unit
) {
    val state = remember {
        mutableStateOf<PaymentState>(
            PaymentState.PaymentMethodList1(
                methods = listOf(
                    PrimerHeadlessUniversalCheckoutPaymentMethod(
                        paymentMethodType = "PAYPAL",
                        "Paypal",
                        supportedPrimerSessionIntents = emptyList(),
                        paymentMethodManagerCategories = emptyList()
                    ),
                    PrimerHeadlessUniversalCheckoutPaymentMethod(
                        paymentMethodType = "CARD",
                        "card",
                        supportedPrimerSessionIntents = emptyList(),
                        paymentMethodManagerCategories = emptyList()
                    )
                )
            )
        )
    }
    BottomSheetScaffold(
        sheetContent = {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                PaymentFlowScopeImpl(
                    state = state.value,
                    onEvent = onEvent,
                    content
                ).Render()
            }
        },
        sheetPeekHeight = 300.dp
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text("Scaffold Content")
        }
    }
}