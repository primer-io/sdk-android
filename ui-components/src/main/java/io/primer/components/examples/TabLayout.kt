package io.primer.components.examples

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.primer.components.ui.PrimerButtonComponent
import io.primer.components.ui.checkout.PrimerCheckout
import kotlinx.coroutines.launch

@Suppress("all")
@Composable
fun TabLayoutCheckout() {
    PrimerCheckout(
        clientToken = "token",
        onPaymentCompleted = { /* Handle completion */ },
    ) { // This is a PaymentFlowScope receiver
        val methods by paymentMethods.collectAsState()
        var selectedTabIndex by remember { mutableStateOf(0) }
        val selectedMethod by selectedMethod.collectAsState()

        Column(modifier = Modifier.fillMaxWidth()) {
            // Custom tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                methods.forEachIndexed { index, method ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            selectPaymentMethod(method)
                        },
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            PrimerButtonComponent(paymentMethod = method) {
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(method.paymentMethodName.orEmpty())
                        }
                    }
                }
            }

            // Content below tabs
            selectedMethod?.let { method ->
                // ModalBottomSheet(onDismissRequest = { /*TODO*/ }) {
                PaymentMethodContent(method) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val coroutine = rememberCoroutineScope()
                            // Payment form

                            @Suppress("UnusedPrivateProperty")
                            var input = remember {
                                mutableStateOf("")
                            }
                            Text(text = "mate")
                            Spacer(Modifier.height(16.dp))

                            DefaultContent()

                            Spacer(Modifier.height(16.dp))

                            // Payment button
                            val contentState by state.collectAsState()
                            Button(
                                onClick = {
                                    coroutine.launch { submit() }
                                },
                                enabled = contentState.validationState.isValid,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                ),
                            ) {
                                Text("Pay with ${method.paymentMethodName.orEmpty()}")
                            }
                        }
                    }
                }
            }
        }
    }
}
