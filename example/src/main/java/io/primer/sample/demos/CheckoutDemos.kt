package io.primer.sample.demos

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import io.primer.android.api.checkout.PrimerCheckoutHost
import io.primer.android.api.checkout.PrimerCheckoutSheet
import io.primer.android.api.checkout.PrimerCheckoutSheetDefaults
import io.primer.android.api.checkout.rememberPrimerCheckoutController
import io.primer.android.api.components.card.PrimerCardForm
import io.primer.android.api.components.card.rememberCardFormController
import io.primer.android.api.components.paymentMethods.PrimerPaymentMethods
import io.primer.android.api.components.paymentMethods.PrimerPaymentMethodsController
import io.primer.android.api.components.paymentMethods.PrimerVaultedPaymentMethods
import io.primer.android.api.components.paymentMethods.rememberPaymentMethodsController
import io.primer.android.api.components.paymentMethods.rememberVaultedPaymentMethodsController
import io.primer.android.api.state.PrimerCheckoutController
import io.primer.android.api.state.PrimerCheckoutState
import io.primer.android.configuration.domain.model.Surcharge
import io.primer.android.core.ExperimentalPrimerApi
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.internal.domain.models.PrimerComposablePaymentMethod
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import kotlinx.coroutines.delay

/**
 * Demo 1: Basic Sheet Checkout
 *
 * Shows the simplest possible integration using the modal bottom sheet
 * with all default UI components.
 */
@OptIn(ExperimentalPrimerApi::class)
@Composable
fun BasicSheetCheckoutDemo(
    clientToken: String,
    settings: PrimerSettings,
    onDismiss: () -> Unit,
) {
    val checkout = rememberPrimerCheckoutController(clientToken, settings)
    val state by checkout.state.collectAsState()
    // Just the sheet - all defaults
    PrimerCheckoutSheet(
        checkout = checkout,
        onDismiss = onDismiss,
    )
}

/**
 * Demo 2: Inline Card Form
 *
 * Shows the CardForm component embedded inline.
 */
@OptIn(ExperimentalPrimerApi::class)
@Composable
fun CustomCardFormSheetDemo(
    clientToken: String,
    settings: PrimerSettings,
    onDismiss: () -> Unit,
) {
    val checkout = rememberPrimerCheckoutController(clientToken, settings)
    val checkoutState by checkout.state.collectAsStateWithLifecycle()
    val isLoading = checkoutState is PrimerCheckoutState.Loading

    // Observe terminal states
    PrimerCheckoutHost(checkout = checkout) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = "Card Form",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            } else {
                val cardFormState = rememberCardFormController(checkout)
                PrimerCardForm(controller = cardFormState)
            }
        }
    }
}

/**
 * Demo 3: Sheet with Custom Payment Method List
 *
 * Shows how to customize how payment methods are displayed
 * with custom items and analytics.
 */
@OptIn(ExperimentalPrimerApi::class)
@Composable
fun CustomPaymentMethodListDemo(
    clientToken: String,
    settings: PrimerSettings,
    onDismiss: () -> Unit,
) {
    val checkout = rememberPrimerCheckoutController(clientToken, settings)
    val state by checkout.state.collectAsState()
    PrimerCheckoutSheet(
        checkout = checkout,
        onDismiss = onDismiss,
        paymentMethodSelection = {
            PrimerCheckoutSheetDefaults.PaymentMethodSelection(
                checkout = checkout,
                paymentMethods = {
                    val paymentMethodState = rememberPaymentMethodsController(checkout)
                    PrimerPaymentMethods(
                        controller = paymentMethodState,
                        method = { method, onClick ->
                            CustomPaymentMethodItem(method, onClick)
                        },
                    )
                },
            )
        },
    )
}

@Composable
private fun CustomPaymentMethodItem(
    method: PrimerComposablePaymentMethod,
    onClick: () -> Unit,
) {
    val type = PaymentMethodType.safeValueOf(method.paymentMethodType)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable {
                // Could add analytics here
                onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = when (type) {
                PaymentMethodType.PAYMENT_CARD -> Color(0xFFE3F2FD)
                PaymentMethodType.PAYPAL -> Color(0xFFFFF8E1)
                PaymentMethodType.GOOGLE_PAY -> Color(0xFFE8F5E9)
                else -> Color.White
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = method.paymentMethodName ?: method.paymentMethodType,
                    fontWeight = FontWeight.Medium,
                )
                val surchargeAmount = when (val s = method.surcharge) {
                    is Surcharge.PaymentMethodSurcharge -> s.amount
                    is Surcharge.CardNetworksSurcharge -> null
                    null -> null
                }
                surchargeAmount?.let { amount ->
                    if (amount > 0) {
                        Text(
                            text = "+$amount",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                        )
                    }
                }
            }
            Text(
                text = "→",
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

/**
 * Demo 4: Inline Checkout (Embedded Card Form)
 *
 * Shows how to embed checkout components directly in your layout
 * without using the modal sheet.
 * Compare with PaymentMethodListOnlyDemo which uses the FLOWSHEET approach:
 * - Uses default onClick for cards
 * - CardForm opens in FlowSheet automatically
 */
@OptIn(ExperimentalPrimerApi::class)
@Composable
fun InlineCheckoutDemo(
    clientToken: String,
    settings: PrimerSettings,
) {
    val checkout = rememberPrimerCheckoutController(clientToken, settings)
    val paymentMethodState = rememberPaymentMethodsController(checkout)
    val checkoutState by checkout.state.collectAsState()
    val isLoading = checkoutState is PrimerCheckoutState.Loading
    var selectedMethod by remember { mutableStateOf<PrimerComposablePaymentMethod?>(null) }

    PrimerCheckoutHost(checkout = checkout) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // Order summary
            OrderSummarySection()

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            } else {
                // Payment methods selection
                Text(
                    text = "Select Payment Method",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                PrimerPaymentMethods(
                    controller = paymentMethodState,
                    method = { method, onClick ->
                        val isSelected = selectedMethod == method
                        val type = PaymentMethodType.safeValueOf(method.paymentMethodType)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    selectedMethod = method
                                    // For cards, we show the form below
                                    // For APMs, this triggers FlowSheet automatically
                                    if (type != PaymentMethodType.PAYMENT_CARD) {
                                        onClick()
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White,
                            ),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = method.paymentMethodName ?: method.paymentMethodType,
                                    modifier = Modifier.weight(1f),
                                )
                                if (isSelected) {
                                    Text("✓", color = Color(0xFF1976D2))
                                }
                            }
                        }
                    },
                )

                // Show card form if card is selected
                val selectedType = selectedMethod?.let { PaymentMethodType.safeValueOf(it.paymentMethodType) }
                if (selectedType == PaymentMethodType.PAYMENT_CARD) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Card Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val cardFormState = rememberCardFormController(checkout)
                    PrimerCardForm(controller = cardFormState)
                }
            }
        }
    }
}

@Composable
private fun OrderSummarySection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Order Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Premium Widget × 1")
                Text("$79.99")
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Shipping")
                Text("Free", color = Color(0xFF4CAF50))
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Total", fontWeight = FontWeight.Bold)
                Text("$79.99", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Demo 5: Vault Management
 *
 * Shows how to display and manage vaulted payment methods.
 */
@OptIn(ExperimentalPrimerApi::class)
@Composable
fun VaultManagementDemo(
    clientToken: String,
    settings: PrimerSettings,
    onDismiss: () -> Unit,
) {
    val checkout = rememberPrimerCheckoutController(clientToken, settings)
    val state by checkout.state.collectAsState()
    PrimerCheckoutSheet(
        checkout = checkout,
        onDismiss = onDismiss,
        paymentMethodSelection = {
            val paymentMethodState = rememberPaymentMethodsController(checkout)
            val vaultedState = rememberVaultedPaymentMethodsController(checkout)
            val vaultedMethods by vaultedState.methods.collectAsState()

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Saved Payment Methods",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (vaultedMethods.isEmpty()) {
                    Text(
                        text = "No saved payment methods",
                        color = Color.Gray,
                    )
                } else {
                    PrimerVaultedPaymentMethods(
                        controller = vaultedState,
                        item = { method, isSelected, onSelect ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                onClick = onSelect,
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        val last4 = method.paymentInstrumentData.last4Digits
                                        val network = method.paymentInstrumentData.network
                                        Text(
                                            text = "${network ?: method.paymentMethodType} •••• ${last4 ?: ""}",
                                            fontWeight = FontWeight.Medium,
                                        )
                                        method.paymentInstrumentData.expirationMonth?.let { month ->
                                            method.paymentInstrumentData.expirationYear?.let { year ->
                                                Text(
                                                    text = "Expires ${month.toString().padStart(2, '0')}/$year",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.Gray,
                                                )
                                            }
                                        }
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                            }
                        },
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                Text(
                    text = "Add New Payment Method",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )

                Spacer(modifier = Modifier.height(8.dp))

                PrimerPaymentMethods(controller = paymentMethodState)
            }
        },
    )
}

/**
 * Demo 6: Inline Vault Mode with Custom Navigation
 *
 * Shows a fully custom inline flow:
 * - Shows payment methods list (vault mode - for saving cards)
 * - When card is clicked, navigates to a separate card form screen
 * - User fills in card details and submits
 * - On completion, returns to payment list and shows the token
 *
 * This demonstrates how to build a completely custom checkout flow
 * using the SDK's components as building blocks.
 *
 * Key pattern: The `onResult` callback is the primary way to handle payment results.
 * We intercept it to update internal navigation state.
 */
@OptIn(ExperimentalPrimerApi::class)
@Composable
fun VaultModeInlineDemo(
    clientToken: String,
    settings: PrimerSettings,
) {
    // Navigation state
    var currentScreen by remember { mutableStateOf<VaultDemoScreen>(VaultDemoScreen.PaymentList) }
    var lastToken by remember { mutableStateOf<String?>(null) }

    val checkout = rememberPrimerCheckoutController(clientToken, settings)
    val paymentMethodState = rememberPaymentMethodsController(checkout)
    val checkoutState by checkout.state.collectAsState()
    val isLoading = checkoutState is PrimerCheckoutState.Loading

    LaunchedEffect(checkoutState) {
        when (val state = checkoutState) {
            is PrimerCheckoutState.Success -> {
                lastToken = state.checkoutData.payment.id
                currentScreen = VaultDemoScreen.PaymentList
            }
            is PrimerCheckoutState.TokenCreated -> {
                lastToken = state.token
                currentScreen = VaultDemoScreen.PaymentList
            }
            is PrimerCheckoutState.Failure, is PrimerCheckoutState.Cancelled -> {
                currentScreen = VaultDemoScreen.PaymentList
            }
            else -> {}
        }
    }

    PrimerCheckoutHost(checkout = checkout) {
        when (currentScreen) {
            is VaultDemoScreen.PaymentList -> {
                VaultPaymentListScreen(
                    paymentMethodState = paymentMethodState,
                    isLoading = isLoading,
                    lastToken = lastToken,
                    onCardSelected = { currentScreen = VaultDemoScreen.CardForm },
                    onPaymentMethodSelected = { method ->
                        // For non-card methods, onClick() triggers FlowSheet automatically
                    },
                )
            }

            is VaultDemoScreen.CardForm -> {
                VaultCardFormScreen(
                    checkout = checkout,
                    onBack = { currentScreen = VaultDemoScreen.PaymentList },
                )
            }
        }
    }
}

/**
 * Screens for the vault mode demo navigation.
 */
private sealed interface VaultDemoScreen {
    data object PaymentList : VaultDemoScreen
    data object CardForm : VaultDemoScreen
}

@OptIn(ExperimentalPrimerApi::class)
@Composable
private fun VaultPaymentListScreen(
    paymentMethodState: PrimerPaymentMethodsController,
    isLoading: Boolean,
    lastToken: String?,
    onCardSelected: () -> Unit,
    onPaymentMethodSelected: (PrimerComposablePaymentMethod) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Vault Mode Demo",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Save a new payment method to your vault",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
        )

        // Show last created token if available
        lastToken?.let { token ->
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Token Created Successfully!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Token: ${token.take(20)}...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF1B5E20),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        } else {
            Text(
                text = "Select a payment method to save:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )

            Spacer(modifier = Modifier.height(12.dp))

            PrimerPaymentMethods(
                controller = paymentMethodState,
                method = { method, onClick ->
                    val type = PaymentMethodType.safeValueOf(method.paymentMethodType)
                    val isCard = type == PaymentMethodType.PAYMENT_CARD

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                if (isCard) {
                                    // Navigate to custom card form screen
                                    onCardSelected()
                                } else {
                                    // For APMs, use normal flow
                                    onPaymentMethodSelected(method)
                                    onClick()
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCard) Color(0xFFE3F2FD) else Color.White,
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = method.paymentMethodName ?: method.paymentMethodType,
                                    fontWeight = FontWeight.Medium,
                                )
                                if (isCard) {
                                    Text(
                                        text = "Tap to enter card details",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                    )
                                }
                            }
                            Text(
                                text = "→",
                                style = MaterialTheme.typography.titleLarge,
                                color = if (isCard) Color(0xFF1976D2) else Color.Gray,
                            )
                        }
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalPrimerApi::class)
@Composable
private fun VaultCardFormScreen(
    checkout: PrimerCheckoutController,
    onBack: () -> Unit,
) {
    val cardFormState = rememberCardFormController(checkout)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        // Back button
        Row(
            modifier = Modifier.clickable { onBack() },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "←",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF1976D2),
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Back to Payment Methods",
                color = Color(0xFF1976D2),
                fontWeight = FontWeight.Medium,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Enter Card Details",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your card will be saved for future payments",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
        )

        Spacer(modifier = Modifier.height(24.dp))

        PrimerCardForm(controller = cardFormState)
    }
}

/**
 * Demo 7: Custom Loading, Success, and Error Screens
 *
 * Shows how to override the loading, success, and error screens with
 * custom implementations that include:
 * - Logging and analytics tracking
 * - Simulated API calls (e.g., order confirmation)
 * - Custom branding and messaging
 * - Error recovery options
 */
@OptIn(ExperimentalPrimerApi::class)
@Composable
fun CustomResultScreensDemo(
    clientToken: String,
    settings: PrimerSettings,
    onDismiss: () -> Unit,
) {
    val checkout = rememberPrimerCheckoutController(clientToken, settings)
    val state by checkout.state.collectAsState()
    PrimerCheckoutSheet(
        checkout = checkout,
        onDismiss = onDismiss,
        loading = {
            CustomLoadingScreen()
        },
        success = { checkoutData ->
            CustomSuccessScreen(checkoutData = checkoutData)
        },
        error = { error ->
            CustomErrorScreen(error = error)
        },
    )
}

/**
 * Custom loading screen with logging.
 */
@Composable
private fun CustomLoadingScreen() {
    // Log when loading screen is shown
    LaunchedEffect(Unit) {
        Log.d("CheckoutDemo", "Loading screen displayed")
        // Simulate analytics event
        trackAnalyticsEvent("checkout_loading_shown")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = Color(0xFF6200EE),
            strokeWidth = 4.dp,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Preparing your checkout...",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "This will only take a moment",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
        )
    }
}

/**
 * Custom success screen with order confirmation API call simulation.
 */
@Composable
private fun CustomSuccessScreen(checkoutData: PrimerCheckoutData) {
    var orderConfirmed by remember { mutableStateOf(false) }
    var confirmationNumber by remember { mutableStateOf<String?>(null) }

    // Simulate order confirmation API call when success screen is shown
    LaunchedEffect(checkoutData) {
        Log.d("CheckoutDemo", "Payment successful: ${checkoutData.payment.id}")
        trackAnalyticsEvent("payment_success", mapOf("payment_id" to checkoutData.payment.id))

        // Simulate API call to confirm order on your backend
        Log.d("CheckoutDemo", "Calling order confirmation API...")
        delay(1500) // Simulate network delay

        // Simulate receiving confirmation number from backend
        confirmationNumber = "ORD-${System.currentTimeMillis().toString().takeLast(8)}"
        orderConfirmed = true

        Log.d("CheckoutDemo", "Order confirmed: $confirmationNumber")
        trackAnalyticsEvent("order_confirmed", mapOf("confirmation" to confirmationNumber!!))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Success icon
        Card(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(40.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Success",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Payment Successful!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Thank you for your purchase",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Order details card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OrderDetailRow("Payment ID", checkoutData.payment.id.take(12) + "...")
                OrderDetailRow("Order ID", checkoutData.payment.orderId.take(12) + "...")

                if (orderConfirmed && confirmationNumber != null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    OrderDetailRow("Confirmation #", confirmationNumber!!)
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Confirming order...",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "A confirmation email has been sent to your inbox",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

@Composable
private fun OrderDetailRow(label: String, value: String) {
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

/**
 * Custom error screen with detailed error info and recovery options.
 */
@Composable
private fun CustomErrorScreen(error: PrimerError) {
    // Log error details
    LaunchedEffect(error) {
        Log.e("CheckoutDemo", "Payment failed: ${error.errorId}")
        Log.e("CheckoutDemo", "Error description: ${error.description}")
        Log.e("CheckoutDemo", "Diagnostics ID: ${error.diagnosticsId}")

        trackAnalyticsEvent(
            "payment_error",
            mapOf(
                "error_id" to error.errorId,
                "diagnostics_id" to error.diagnosticsId,
                "error_code" to (error.errorCode ?: "unknown"),
            ),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Error icon
        Card(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(40.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "✕",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFFD32F2F),
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Payment Failed",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD32F2F),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = error.description,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )

        // Recovery suggestion if available
        error.recoverySuggestion?.let { suggestion ->
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = "💡",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE65100),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Debug info (collapsible in production)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Debug Info",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Error ID: ${error.errorId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                )
                Text(
                    text = "Diagnostics: ${error.diagnosticsId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                )
                error.errorCode?.let { code ->
                    Text(
                        text = "Code: $code",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                    )
                }
            }
        }
    }
}

/**
 * Simulated analytics tracking function.
 * In a real app, this would call your analytics service (e.g., Amplitude, Mixpanel, Firebase).
 */
private fun trackAnalyticsEvent(event: String, properties: Map<String, String> = emptyMap()) {
    Log.d("Analytics", "Event: $event, Properties: $properties")
    // In production:
    // analytics.track(event, properties)
}
