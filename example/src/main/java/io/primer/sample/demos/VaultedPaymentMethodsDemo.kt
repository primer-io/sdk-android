package io.primer.sample.demos

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.android.api.checkout.PrimerCheckoutHost
import io.primer.android.api.state.PrimerCheckoutState
import io.primer.android.api.components.paymentMethods.PrimerVaultedPaymentMethods
import io.primer.android.api.components.paymentMethods.rememberVaultedPaymentMethodsController
import io.primer.android.api.checkout.rememberPrimerCheckoutController
import io.primer.android.core.ExperimentalPrimerApi
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod

/**
 * Demo: Vaulted Payment Methods
 *
 * This demo shows how to work with saved/vaulted payment methods:
 *
 * 1. Basic Usage - Using VaultedPaymentMethods component with defaults
 * 2. State Observation - Listening to methods StateFlow for custom UI
 * 3. Custom Item Rendering - Swipe-to-delete cards with custom design
 * 4. Actions - select(), delete(), showAll()
 *
 * Key concepts:
 * - rememberVaultedPaymentMethodState(checkout) creates the state
 * - state.methods is a StateFlow<List<PrimerVaultedPaymentMethod>>
 * - state.select(method) triggers payment (shows CVV if needed)
 * - state.delete(method) shows confirmation and removes card
 * - state.showAll() opens full vault management screen
 */
@OptIn(ExperimentalPrimerApi::class)
@Composable
fun VaultedPaymentMethodsDemo(
    clientToken: String,
    settings: PrimerSettings,
) {
    val checkout = rememberPrimerCheckoutController(clientToken, settings)
    val checkoutState by checkout.state.collectAsStateWithLifecycle()
    PrimerCheckoutHost(checkout = checkout) {
        if (checkoutState is PrimerCheckoutState.Loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                Text(
                    text = "Vaulted Payment Methods Demo",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Example 1: Default component
                DefaultVaultedPaymentMethodsExample(checkout)

                Spacer(modifier = Modifier.height(32.dp))

                // Example 2: Custom state-driven UI
                CustomVaultedCardsExample(checkout)
            }
        }
    }
}

/**
 * Example 1: Using the default VaultedPaymentMethods component
 *
 * This is the simplest approach - just pass the state and it handles everything.
 */
@OptIn(ExperimentalPrimerApi::class)
@Composable
private fun DefaultVaultedPaymentMethodsExample(checkout: io.primer.android.api.state.PrimerCheckoutController) {
    val vaultedState = rememberVaultedPaymentMethodsController(checkout)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "1. Default Component",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "VaultedPaymentMethods with default styling",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Default usage - this handles everything
            PrimerVaultedPaymentMethods(controller = vaultedState)
        }
    }
}

/**
 * Example 2: Custom UI by observing state directly
 *
 * This shows how to build completely custom UI while using the SDK's state management.
 * Great for when you need full control over the design.
 */
@OptIn(ExperimentalPrimerApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun CustomVaultedCardsExample(checkout: io.primer.android.api.state.PrimerCheckoutController) {
    val vaultedState = rememberVaultedPaymentMethodsController(checkout)
    val methods by vaultedState.methods.collectAsStateWithLifecycle()

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
                Column {
                    Text(
                        text = "2. Custom State-Driven UI",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Swipe to delete, tap to pay",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                    )
                }

                // Manage button - opens full vault screen
                IconButton(onClick = { vaultedState.showAll() }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Manage cards",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (methods.isEmpty()) {
                Text(
                    text = "No saved payment methods",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 24.dp),
                )
            } else {
                // Custom list with swipe-to-delete
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(200.dp), // Fixed height for demo
                ) {
                    items(
                        items = methods,
                        key = { it.id },
                    ) { method ->
                        SwipeToDeleteCard(
                            method = method,
                            onSelect = { vaultedState.select(method) },
                            onDelete = { vaultedState.delete(method) },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Custom swipe-to-delete card item
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteCard(
    method: PrimerVaultedPaymentMethod,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                false // Don't dismiss, let the delete confirmation handle it
            } else {
                false
            }
        },
    )

    val backgroundColor by animateColorAsState(
        targetValue = when (dismissState.targetValue) {
            SwipeToDismissBoxValue.EndToStart -> Color(0xFFFFEBEE)
            else -> Color.Transparent
        },
        label = "swipe_background",
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFE53935),
                )
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelect() },
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Card icon
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color.Gray,
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "•••• ${method.paymentInstrumentData.last4Digits}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = method.paymentInstrumentData.network ?: method.paymentMethodType,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                    )
                }

                // Expiry
                method.paymentInstrumentData.expirationMonth?.let { month ->
                    method.paymentInstrumentData.expirationYear?.let { year ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFF0F0F0))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = "${month.toString().padStart(2, '0')}/$year",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                            )
                        }
                    }
                }
            }
        }
    }
}
