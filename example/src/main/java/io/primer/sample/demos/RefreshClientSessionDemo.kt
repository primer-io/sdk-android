package io.primer.sample.demos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.android.api.checkout.PrimerCheckoutHost
import io.primer.android.api.checkout.rememberPrimerCheckoutController
import io.primer.android.api.components.paymentMethods.PrimerPaymentMethods
import io.primer.android.api.components.paymentMethods.PrimerVaultedPaymentMethods
import io.primer.android.api.components.paymentMethods.rememberPaymentMethodsController
import io.primer.android.api.components.paymentMethods.rememberVaultedPaymentMethodsController
import io.primer.android.api.state.PrimerCheckoutState
import io.primer.android.api.state.formatAmount
import io.primer.android.core.ExperimentalPrimerApi
import io.primer.android.data.settings.PrimerSettings

/**
 * Demo: Refresh Client Session
 *
 * This demo tests whether `checkout.refreshClientSession()` properly updates:
 * 1. The checkout state (isLoading, clientSession data)
 * 2. Payment methods list (via StateFlow)
 * 3. Vaulted payment methods list (via StateFlow)
 *
 * Use this to verify that all observers receive updated data after refresh.
 *
 * Scenario: After updating an order on your server (e.g., adding items),
 * call refreshClientSession() to fetch the new amount and payment methods.
 */
@OptIn(ExperimentalPrimerApi::class)
@Composable
fun RefreshClientSessionDemo(
    clientToken: String,
    settings: PrimerSettings,
) {
    val checkout = rememberPrimerCheckoutController(clientToken, settings)
    val checkoutState by checkout.state.collectAsStateWithLifecycle()
    // Track refresh count to show it's actually refreshing
    var refreshCount by remember { mutableIntStateOf(0) }

    PrimerCheckoutHost(checkout = checkout) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Text(
                text = "Refresh Client Session Demo",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = "Test that refreshClientSession() updates all observers",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Session State Card
            SessionStateCard(
                isLoading = checkoutState is PrimerCheckoutState.Loading,
                error = (checkoutState as? PrimerCheckoutState.Failure)?.error?.description,
                totalAmount = (checkoutState as? PrimerCheckoutState.Ready)?.clientSession?.totalAmount,
                currency = (checkoutState as? PrimerCheckoutState.Ready)?.clientSession?.currencyCode,
                formattedAmount = (checkoutState as? PrimerCheckoutState.Ready)?.clientSession?.totalAmount?.let {
                    checkout.formatAmount(it)
                },
                refreshCount = refreshCount,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Refresh Button
            Button(
                onClick = {
                    refreshCount++
                    checkout.refresh()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = checkoutState !is PrimerCheckoutState.Loading,
            ) {
                if (checkoutState is PrimerCheckoutState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Refresh Client Session")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(24.dp))

            // Payment Methods Section
            PaymentMethodsObserverCard(checkout)

            Spacer(modifier = Modifier.height(16.dp))

            // Vaulted Payment Methods Section
            VaultedMethodsObserverCard(checkout)
        }
    }
}

@Composable
private fun SessionStateCard(
    isLoading: Boolean,
    error: String?,
    totalAmount: Int?,
    currency: String?,
    formattedAmount: String?,
    refreshCount: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (error != null) Color(0xFFFFEBEE) else Color(0xFFE3F2FD),
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Checkout State",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(12.dp))

            StateRow("isLoading", if (isLoading) "true ⏳" else "false ✓")
            StateRow("error", error ?: "null")
            StateRow("totalAmount", totalAmount?.toString() ?: "null")
            StateRow("currency", currency ?: "null")
            StateRow("formatted", formattedAmount ?: "—")
            StateRow("refreshCount", refreshCount.toString())
        }
    }
}

@Composable
private fun StateRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@OptIn(ExperimentalPrimerApi::class)
@Composable
private fun PaymentMethodsObserverCard(checkout: io.primer.android.api.state.PrimerCheckoutController) {
    val paymentMethodsController = rememberPaymentMethodsController(checkout)
    val methods by paymentMethodsController.paymentMethods.collectAsStateWithLifecycle()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Payment Methods",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${methods.size} available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (methods.isEmpty()) {
                Text(
                    text = "No payment methods loaded yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                )
            } else {
                Text(
                    text = "Types: ${methods.joinToString { it.paymentMethodType }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Show actual payment method list
            PrimerPaymentMethods(controller = paymentMethodsController)
        }
    }
}

@OptIn(ExperimentalPrimerApi::class)
@Composable
private fun VaultedMethodsObserverCard(checkout: io.primer.android.api.state.PrimerCheckoutController) {
    val vaultedState = rememberVaultedPaymentMethodsController(checkout)
    val methods by vaultedState.methods.collectAsStateWithLifecycle()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Vaulted Payment Methods",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${methods.size} saved",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (methods.isEmpty()) {
                Text(
                    text = "No saved payment methods",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                )
            } else {
                Text(
                    text = "Cards: ${methods.joinToString { "****${it.paymentInstrumentData.last4Digits}" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Show actual vaulted methods component
                PrimerVaultedPaymentMethods(controller = vaultedState)
            }
        }
    }
}
