package io.primer.sample.demos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.android.api.components.paymentMethods.PaymentMethodsDefaults
import io.primer.android.api.state.PrimerCheckoutController
import io.primer.android.api.checkout.PrimerCheckoutHost
import io.primer.android.api.state.PrimerCheckoutState
import io.primer.android.api.state.formatAmount
import io.primer.android.api.components.paymentMethods.rememberPaymentMethodsController
import io.primer.android.api.checkout.rememberPrimerCheckoutController
import io.primer.android.core.ExperimentalPrimerApi
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.internal.domain.models.PrimerComposablePaymentMethod

/**
 * Demo: Inline Payment Methods with Radio Selection
 *
 * This demo shows how the SDK handles flows automatically via FlowSheet:
 * - Payment methods displayed as radio buttons
 * - Floating "Pay" button appears when a method is selected
 * - Calling `paymentMethodState.select(method)` triggers the appropriate flow:
 *   - For cards: SDK automatically shows CardForm in FlowSheet
 *   - For APMs: SDK automatically shows the native flow in FlowSheet
 *
 * Key insight: The merchant doesn't need to manually manage card form modals
 * or APM flows. Just call `select()` and the SDK takes over!
 */
@OptIn(ExperimentalPrimerApi::class)
@Composable
fun RadioSelectionDemo(
    clientToken: String,
    settings: PrimerSettings,
) {
    var selectedMethod by remember { mutableStateOf<PrimerComposablePaymentMethod?>(null) }

    val checkout = rememberPrimerCheckoutController(clientToken, settings)
    val paymentMethodState = rememberPaymentMethodsController(checkout)
    val methods by paymentMethodState.paymentMethods.collectAsStateWithLifecycle()
    val checkoutState by checkout.state.collectAsStateWithLifecycle()
    val isLoading = checkoutState is PrimerCheckoutState.Loading
    PrimerCheckoutHost(checkout = checkout) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 100.dp), // Space for floating button
            ) {
                // Order Summary
                RadioDemoOrderSummary(checkout)

                Spacer(modifier = Modifier.height(24.dp))

                // Payment Methods Section
                Text(
                    text = "Select Payment Method",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(32.dp),
                    )
                } else {
                    // Payment methods as radio group using remembered methods
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
                        Column {
                            methods.forEach { method ->
                                PaymentMethodRadioRow(
                                    method = method,
                                    isSelected = selectedMethod == method,
                                    onSelected = { selectedMethod = method },
                                )
                            }
                        }
                    }
                }
            }

            // Floating Pay Button - appears when a method is selected
            AnimatedVisibility(
                visible = selectedMethod != null,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = Color.White,
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        selectedMethod?.let { method ->
                            Text(
                                text = "Selected: ${method.paymentMethodName ?: method.paymentMethodType}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            PaymentMethodsDefaults.Method(method, onClick = {
                                paymentMethodState.select(method)
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodRadioRow(
    method: PrimerComposablePaymentMethod,
    isSelected: Boolean,
    onSelected: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelected,
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = method.paymentMethodName ?: method.paymentMethodType,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            )
            // Show payment method type for context
            if (method.paymentMethodName != null) {
                Text(
                    text = method.paymentMethodType,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                )
            }
        }
    }
}

@Composable
private fun RadioDemoOrderSummary(checkout: PrimerCheckoutController) {
    val state by checkout.state.collectAsStateWithLifecycle()
    val clientSession = (state as? PrimerCheckoutState.Ready)?.clientSession

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Order Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Premium Subscription", color = Color.DarkGray)
                Text("$29.99/mo")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("First month discount", color = Color.DarkGray)
                Text("-$10.00", color = Color(0xFF4CAF50))
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Total due today", fontWeight = FontWeight.Bold)
                clientSession?.totalAmount?.let { Text(checkout.formatAmount(it), fontWeight = FontWeight.Bold) }
            }
        }
    }
}
