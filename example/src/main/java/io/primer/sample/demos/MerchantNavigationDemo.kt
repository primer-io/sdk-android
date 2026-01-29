package io.primer.sample.demos

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.primer.android.api.components.card.PrimerCardForm
import io.primer.android.api.components.paymentMethods.PrimerPaymentMethods
import androidx.compose.runtime.LaunchedEffect
import io.primer.android.api.components.paymentMethods.PrimerPaymentMethodsController
import io.primer.android.api.state.PrimerCheckoutController
import io.primer.android.api.checkout.PrimerCheckoutHost
import io.primer.android.api.state.PrimerCheckoutState
import io.primer.android.api.components.paymentMethods.PrimerVaultedPaymentMethodsController
import io.primer.android.api.components.paymentMethods.PrimerVaultedPaymentMethods
import io.primer.android.api.components.card.rememberCardFormController
import io.primer.android.api.components.paymentMethods.rememberPaymentMethodsController
import io.primer.android.api.checkout.rememberPrimerCheckoutController
import io.primer.android.api.components.paymentMethods.rememberVaultedPaymentMethodsController
import io.primer.android.core.ExperimentalPrimerApi
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType

/**
 * Demo: Merchant-Controlled Navigation
 *
 * This demo shows a complete checkout experience with merchant-controlled navigation:
 *
 * 1. **Checkout Tab**: Shows available payment methods
 *    - Clicking on Card navigates to a separate card form screen
 *    - Other payment methods trigger their flows immediately
 *
 * 2. **Profile Tab**: Shows user profile with vaulted payment methods
 *    - Displays saved cards with last 4 digits and expiry
 *    - Allows quick payment with saved methods
 *    - Allows deletion of saved methods
 *
 * Key patterns demonstrated:
 * - Multi-screen navigation within inline checkout
 * - Separate screens for payment methods and card form
 * - Profile/vault management as a separate tab
 * - Custom back navigation handling
 */
@OptIn(ExperimentalPrimerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MerchantNavigationDemo(
    clientToken: String,
    settings: PrimerSettings,
) {
    // Tab navigation
    var selectedTab by remember { mutableStateOf(MerchantTab.Checkout) }

    // Screen navigation within Checkout tab
    var checkoutScreen by remember { mutableStateOf<CheckoutScreen>(CheckoutScreen.PaymentMethods) }

    val checkout = rememberPrimerCheckoutController(clientToken, settings)
    val checkoutState by checkout.state.collectAsState()

    LaunchedEffect(checkoutState) {
        when (checkoutState) {
            is PrimerCheckoutState.Success, is PrimerCheckoutState.TokenCreated -> {
                checkoutScreen = CheckoutScreen.PaymentMethods
            }
            is PrimerCheckoutState.Failure, is PrimerCheckoutState.Cancelled -> {
                // Navigate back to payment methods on error/cancellation
            }
            else -> {}
        }
    }

    val paymentMethodState = rememberPaymentMethodsController(checkout)
    val vaultedState = rememberVaultedPaymentMethodsController(checkout)

    PrimerCheckoutHost(checkout = checkout) {
        Scaffold(
            topBar = {
                MerchantTopBar(
                    currentTab = selectedTab,
                    currentScreen = checkoutScreen,
                    onBack = {
                        if (checkoutScreen != CheckoutScreen.PaymentMethods) {
                            checkoutScreen = CheckoutScreen.PaymentMethods
                        }
                    },
                )
            },
            bottomBar = {
                MerchantBottomNavigation(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        selectedTab = tab
                        // Reset checkout screen when switching tabs
                        if (tab == MerchantTab.Checkout) {
                            checkoutScreen = CheckoutScreen.PaymentMethods
                        }
                    },
                )
            },
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "tab_content",
                ) { tab ->
                    when (tab) {
                        MerchantTab.Checkout -> {
                            CheckoutTabContent(
                                checkout = checkout,
                                paymentMethodState = paymentMethodState,
                                currentScreen = checkoutScreen,
                                onNavigateToCardForm = {
                                    checkoutScreen = CheckoutScreen.CardForm
                                },
                                onBack = {
                                    checkoutScreen = CheckoutScreen.PaymentMethods
                                },
                            )
                        }
                        MerchantTab.Profile -> {
                            ProfileTabContent(
                                checkout = checkout,
                                vaultedState = vaultedState,
                            )
                        }
                    }
                }
            }
        }
    }
}

private enum class MerchantTab {
    Checkout,
    Profile,
}

private sealed interface CheckoutScreen {
    data object PaymentMethods : CheckoutScreen
    data object CardForm : CheckoutScreen
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MerchantTopBar(
    currentTab: MerchantTab,
    currentScreen: CheckoutScreen,
    onBack: () -> Unit,
) {
    val showBack = currentTab == MerchantTab.Checkout && currentScreen != CheckoutScreen.PaymentMethods

    TopAppBar(
        title = {
            Text(
                text = when {
                    currentTab == MerchantTab.Profile -> "My Account"
                    currentScreen == CheckoutScreen.CardForm -> "Card Details"
                    else -> "Checkout"
                },
                fontWeight = FontWeight.Bold,
            )
        },
        navigationIcon = {
            if (showBack) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
            }
        },
    )
}

@Composable
private fun MerchantBottomNavigation(
    selectedTab: MerchantTab,
    onTabSelected: (MerchantTab) -> Unit,
) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedTab == MerchantTab.Checkout,
            onClick = { onTabSelected(MerchantTab.Checkout) },
            icon = {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Checkout",
                )
            },
            label = { Text("Checkout") },
        )
        NavigationBarItem(
            selected = selectedTab == MerchantTab.Profile,
            onClick = { onTabSelected(MerchantTab.Profile) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                )
            },
            label = { Text("Profile") },
        )
    }
}

@OptIn(ExperimentalPrimerApi::class)
@Composable
private fun CheckoutTabContent(
    checkout: PrimerCheckoutController,
    paymentMethodState: PrimerPaymentMethodsController,
    currentScreen: CheckoutScreen,
    onNavigateToCardForm: () -> Unit,
    onBack: () -> Unit,
) {
    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            if (targetState == CheckoutScreen.CardForm) {
                // Navigate forward
                slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
            } else {
                // Navigate back
                slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
            }
        },
        label = "checkout_screen",
    ) { screen ->
        when (screen) {
            CheckoutScreen.PaymentMethods -> {
                PaymentMethodsScreen(
                    checkout = checkout,
                    paymentMethodState = paymentMethodState,
                    onCardSelected = onNavigateToCardForm,
                )
            }
            CheckoutScreen.CardForm -> {
                CardFormScreen(
                    checkout = checkout,
                    onBack = onBack,
                )
            }
        }
    }
}

@OptIn(ExperimentalPrimerApi::class)
@Composable
private fun PaymentMethodsScreen(
    checkout: PrimerCheckoutController,
    paymentMethodState: PrimerPaymentMethodsController,
    onCardSelected: () -> Unit,
) {
    val checkoutState by checkout.state.collectAsState()
    val isLoading = checkoutState is PrimerCheckoutState.Loading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        // Order info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Order #12345",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Total: $149.99",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2),
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "How would you like to pay?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(32.dp),
            )
        } else {
            PrimerPaymentMethods(
                controller = paymentMethodState,
                method = { method, onClick ->
                    val type = PaymentMethodType.safeValueOf(method.paymentMethodType)
                    val isCard = type == PaymentMethodType.PAYMENT_CARD

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable {
                                if (isCard) {
                                    // Navigate to card form screen
                                    onCardSelected()
                                } else {
                                    // For other payment methods, start the flow
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
                            // Icon placeholder
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE0E0E0)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = getPaymentMethodEmoji(type),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = method.paymentMethodName ?: method.paymentMethodType,
                                    fontWeight = FontWeight.Medium,
                                )
                                if (isCard) {
                                    Text(
                                        text = "Enter card details",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                    )
                                }
                            }

                            Text(
                                text = "→",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.Gray,
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
private fun CardFormScreen(
    checkout: PrimerCheckoutController,
    onBack: () -> Unit,
) {
    val cardFormController = rememberCardFormController(checkout)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        // Security badge
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "🔒", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Your payment is secure and encrypted",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF2E7D32),
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Card form
        PrimerCardForm(controller = cardFormController)
    }
}

@OptIn(ExperimentalPrimerApi::class)
@Composable
private fun ProfileTabContent(
    checkout: PrimerCheckoutController,
    vaultedState: PrimerVaultedPaymentMethodsController,
) {
    val vaultedMethods by vaultedState.methods.collectAsState()
    val checkoutState by checkout.state.collectAsState()
    val isLoading = checkoutState is PrimerCheckoutState.Loading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        // User info header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1976D2)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "JD",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "John Doe",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "john.doe@example.com",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Saved Payment Methods section
        Text(
            text = "Saved Payment Methods",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(32.dp),
            )
        } else if (vaultedMethods.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "💳",
                        style = MaterialTheme.typography.displayMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No saved payment methods",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Payment methods you use will appear here",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                    )
                }
            }
        } else {
            PrimerVaultedPaymentMethods(
                controller = vaultedState,
                item = { method, isSelected, onSelect ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .then(
                                if (isSelected) {
                                    Modifier.border(2.dp, Color(0xFF1976D2), RoundedCornerShape(8.dp))
                                } else {
                                    Modifier
                                },
                            ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        onClick = onSelect,
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // Card icon
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFE3F2FD)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(text = "💳")
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    val network = method.paymentInstrumentData.network ?: "Card"
                                    val last4 = method.paymentInstrumentData.last4Digits ?: "****"
                                    Text(
                                        text = "$network •••• $last4",
                                        fontWeight = FontWeight.Medium,
                                    )

                                    val expMonth = method.paymentInstrumentData.expirationMonth
                                    val expYear = method.paymentInstrumentData.expirationYear
                                    if (expMonth != null && expYear != null) {
                                        Text(
                                            text = "Expires ${expMonth.toString().padStart(2, '0')}/$expYear",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray,
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color(0xFF1976D2),
                                    )
                                }
                            }
                        }
                    }
                },
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Account settings section (placeholder)
        Text(
            text = "Account Settings",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(12.dp))

        listOf(
            "Notification Preferences" to "Manage alerts",
            "Privacy Settings" to "Control your data",
            "Help & Support" to "Get assistance",
        ).forEach { (title, subtitle) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { /* Navigate to setting */ },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = title, fontWeight = FontWeight.Medium)
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                        )
                    }
                    Text("→", color = Color.Gray)
                }
            }
        }
    }
}

private fun getPaymentMethodEmoji(type: PaymentMethodType): String = when (type) {
    PaymentMethodType.PAYMENT_CARD -> "💳"
    PaymentMethodType.PAYPAL -> "🅿️"
    PaymentMethodType.GOOGLE_PAY -> "🔵"
    PaymentMethodType.KLARNA -> "🟣"
    else -> "💰"
}
