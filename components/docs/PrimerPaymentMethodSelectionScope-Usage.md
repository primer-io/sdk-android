# PrimerPaymentMethodSelectionScope Usage Guide

## Overview

`PrimerPaymentMethodSelectionScope` provides a complete payment method selection interface for the Primer SDK. This scope handles loading available payment methods, displaying order information, and managing user selection with comprehensive customization options.

### Key Capabilities

- **State Management**: Reactive state handling for loading, ready, and error states
- **Payment Method Discovery**: Automatic loading of available payment methods from configuration
- **Order Information**: Display of order details and pricing information
- **UI Customization**: Full screen and individual component customization
- **Navigation Integration**: Seamless routing to selected payment method flows
- **Error Handling**: Built-in error states with retry mechanisms
- **Accessibility**: Comprehensive accessibility support for payment method selection

## Customization Levels

### 1. Screen Level Customization
Replace the entire payment method selection screen with your custom implementation while maintaining the underlying business logic.

### 2. Component Level Customization
Customize individual UI components like payment method cards while keeping the default screen structure.

### 3. Hybrid Customization
Mix and match default components with custom ones for targeted customization where needed.

## How Customization Works

The `PrimerPaymentMethodSelectionScope` uses **property reassignment** for customization:

```kotlin
// ✅ Correct - Property reassignment
paymentMethodSelectionScope.paymentMethodCard = { modifier ->
    CustomPaymentMethodCard(modifier)
}

// ❌ Incorrect - Method override not supported
override fun paymentMethodCard() = CustomPaymentMethodCard()
```

## Basic Usage

### Default Implementation

The simplest way to use `PrimerPaymentMethodSelectionScope` is with the default implementation:

```kotlin
@Composable
fun MyCheckoutScreen() {
    val checkoutScope = Primer.checkout()
    
    // Payment method selection screen with default UI
    LaunchedEffect(Unit) {
        // The scope handles everything automatically:
        // - Loading available payment methods
        // - Displaying order information
        // - Payment method selection
        // - Navigation to selected payment flow
        // - Error handling and loading states
    }
    
    // Access the scope directly
    val paymentMethodScope = checkoutScope.paymentMethodSelection
    val state by paymentMethodScope.state.collectAsState()
    
    when (state) {
        is PrimerPaymentMethodSelectionScope.State.Loading -> {
            // Loading state is handled automatically
        }
        is PrimerPaymentMethodSelectionScope.State.Ready -> {
            // Ready state with payment methods is handled automatically
        }
        is PrimerPaymentMethodSelectionScope.State.Error -> {
            // Error state is handled automatically
        }
    }
}
```

## Component-Level Customization

### Custom Payment Method Card

```kotlin
@Composable
fun MyCheckoutScreen() {
    val checkoutScope = Primer.checkout()
    
    // Customize individual payment method cards
    checkoutScope.paymentMethodSelection.paymentMethodCard = { modifier ->
        CustomPaymentMethodCard(modifier = modifier)
    }
}

@Composable
fun CustomPaymentMethodCard(modifier: Modifier = Modifier) {
    val checkoutScope = Primer.checkout()
    val paymentMethodScope = checkoutScope.paymentMethodSelection
    val state by paymentMethodScope.state.collectAsState()
    
    when (state) {
        is PrimerPaymentMethodSelectionScope.State.Ready -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.paymentMethods) { paymentMethod ->
                    EnhancedPaymentMethodCard(
                        paymentMethod = paymentMethod,
                        onSelect = { paymentMethodScope.onPaymentMethodSelected(paymentMethod.paymentMethodType) }
                    )
                }
            }
        }
        else -> {
            // Loading and error states handled by default screen
        }
    }
}

@Composable
fun EnhancedPaymentMethodCard(
    paymentMethod: PrimerComposablePaymentMethod,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Payment method icon
            PaymentMethodIcon(
                paymentMethodType = paymentMethod.paymentMethodType,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Payment method details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = paymentMethod.paymentMethodName ?: paymentMethod.paymentMethodType,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                // Show surcharge if applicable
                paymentMethod.surcharge?.let { surcharge ->
                    Text(
                        text = "Additional fee: ${formatCurrency(surcharge.amount, surcharge.currency)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Show supported session types
                Text(
                    text = "Supports: ${paymentMethod.supportedPrimerSessionIntents.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Selection arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Select ${paymentMethod.paymentMethodName}",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun PaymentMethodIcon(
    paymentMethodType: String,
    modifier: Modifier = Modifier
) {
    val iconRes = when (paymentMethodType) {
        "PAYMENT_CARD" -> Icons.Default.CreditCard
        "GOOGLE_PAY" -> Icons.Default.Android // Use actual Google Pay icon
        "PAYPAL" -> Icons.Default.Payment // Use actual PayPal icon
        "KLARNA" -> Icons.Default.ShoppingCart // Use actual Klarna icon
        else -> Icons.Default.Payment
    }
    
    Icon(
        imageVector = iconRes,
        contentDescription = paymentMethodType,
        modifier = modifier,
        tint = MaterialTheme.colorScheme.primary
    )
}

fun formatCurrency(amount: Double, currency: String): String {
    return "$${String.format("%.2f", amount)} $currency"
}
```

## Complete Custom Implementation

### Full Screen Replacement

```kotlin
@Composable
fun MyCheckoutScreen() {
    val checkoutScope = Primer.checkout()
    
    // Replace the entire payment method selection screen
    checkoutScope.paymentMethodSelection.screen = {
        CustomPaymentMethodSelectionScreen(checkoutScope.paymentMethodSelection)
    }
}

@Composable
fun CustomPaymentMethodSelectionScreen(scope: PrimerPaymentMethodSelectionScope) {
    val state by scope.state.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (state) {
            is PrimerPaymentMethodSelectionScope.State.Loading -> {
                CustomLoadingScreen()
            }
            is PrimerPaymentMethodSelectionScope.State.Ready -> {
                CustomReadyScreen(
                    paymentMethods = state.paymentMethods,
                    orderInfo = state.orderInfo,
                    onPaymentMethodSelected = scope::onPaymentMethodSelected,
                    onCancel = scope::onCancel
                )
            }
            is PrimerPaymentMethodSelectionScope.State.Error -> {
                CustomErrorScreen(
                    error = state.exception,
                    onRetry = { /* Retry logic */ },
                    onCancel = scope::onCancel
                )
            }
        }
    }
}

@Composable
fun CustomLoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Loading payment methods...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CustomReadyScreen(
    paymentMethods: List<PrimerComposablePaymentMethod>,
    orderInfo: BasicOrderInfo?,
    onPaymentMethodSelected: (String) -> Unit,
    onCancel: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Custom header with order information
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 4.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Payment Method",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onCancel) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                orderInfo?.let { order ->
                    Spacer(modifier = Modifier.height(16.dp))
                    OrderSummaryCard(orderInfo = order)
                }
            }
        }
        
        // Payment methods list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Choose your preferred payment method:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            items(paymentMethods) { paymentMethod ->
                PremiumPaymentMethodCard(
                    paymentMethod = paymentMethod,
                    onSelect = { onPaymentMethodSelected(paymentMethod.paymentMethodType) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(20.dp))
                SecurityNotice()
            }
        }
    }
}

@Composable
fun OrderSummaryCard(orderInfo: BasicOrderInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Order Summary",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Amount:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = formatOrderTotal(orderInfo),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun PremiumPaymentMethodCard(
    paymentMethod: PrimerComposablePaymentMethod,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Enhanced payment method icon with background
            Surface(
                modifier = Modifier.size(60.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    PaymentMethodIcon(
                        paymentMethodType = paymentMethod.paymentMethodType,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            // Payment method details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = paymentMethod.paymentMethodName ?: formatPaymentMethodName(paymentMethod.paymentMethodType),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Payment method description
                Text(
                    text = getPaymentMethodDescription(paymentMethod.paymentMethodType),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Show surcharge if applicable
                paymentMethod.surcharge?.let { surcharge ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Additional fee: ${formatCurrency(surcharge.amount, surcharge.currency)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // Selection indicator
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Select ${paymentMethod.paymentMethodName}",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun SecurityNotice() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Security,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = "Secure Payment",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Your payment information is encrypted and secure",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CustomErrorScreen(
    error: Throwable,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Text(
            text = "Unable to Load Payment Methods",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "We're having trouble loading your payment options. Please check your connection and try again.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
            
            Button(
                onClick = onRetry,
                modifier = Modifier.weight(1f)
            ) {
                Text("Retry")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Error: ${error.message}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

fun formatPaymentMethodName(paymentMethodType: String): String {
    return when (paymentMethodType) {
        "PAYMENT_CARD" -> "Credit or Debit Card"
        "GOOGLE_PAY" -> "Google Pay"
        "PAYPAL" -> "PayPal"
        "KLARNA" -> "Klarna"
        "IDEAL" -> "iDEAL"
        else -> paymentMethodType.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
    }
}

fun getPaymentMethodDescription(paymentMethodType: String): String {
    return when (paymentMethodType) {
        "PAYMENT_CARD" -> "Visa, Mastercard, American Express, and more"
        "GOOGLE_PAY" -> "Pay with your Google account"
        "PAYPAL" -> "Pay with your PayPal account"
        "KLARNA" -> "Buy now, pay later options"
        "IDEAL" -> "Direct bank transfer (Netherlands)"
        else -> "Secure payment method"
    }
}

fun formatOrderTotal(orderInfo: BasicOrderInfo): String {
    // Implementation depends on BasicOrderInfo structure
    return "Total amount" // Placeholder
}
```

## Advanced Usage Patterns

### State Observation and Custom Logic

```kotlin
@Composable
fun MyCheckoutScreen() {
    val checkoutScope = Primer.checkout()
    val paymentMethodScope = checkoutScope.paymentMethodSelection
    val state by paymentMethodScope.state.collectAsState()
    
    // Custom logic based on state
    LaunchedEffect(state) {
        when (state) {
            is PrimerPaymentMethodSelectionScope.State.Ready -> {
                // Analytics tracking
                Analytics.track("payment_methods_loaded", mapOf(
                    "count" to state.paymentMethods.size,
                    "types" to state.paymentMethods.map { it.paymentMethodType }
                ))
            }
            is PrimerPaymentMethodSelectionScope.State.Error -> {
                // Error tracking
                Analytics.track("payment_methods_load_error", mapOf(
                    "error" to state.exception.message
                ))
            }
        }
    }
    
    // Custom screen with state-dependent behavior
    paymentMethodScope.screen = {
        CustomPaymentMethodScreenWithAnalytics(paymentMethodScope, state)
    }
}

@Composable
fun CustomPaymentMethodScreenWithAnalytics(
    scope: PrimerPaymentMethodSelectionScope,
    state: PrimerPaymentMethodSelectionScope.State
) {
    // Track screen view
    LaunchedEffect(Unit) {
        Analytics.track("payment_method_selection_screen_viewed")
    }
    
    // Custom screen implementation with analytics
    CustomPaymentMethodSelectionScreenWithAnalytics(scope)
}

@Composable
fun CustomPaymentMethodSelectionScreenWithAnalytics(scope: PrimerPaymentMethodSelectionScope) {
    val state by scope.state.collectAsState()
    
    when (state) {
        is PrimerPaymentMethodSelectionScope.State.Ready -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.paymentMethods) { paymentMethod ->
                    PaymentMethodCardWithAnalytics(
                        paymentMethod = paymentMethod,
                        onSelect = {
                            Analytics.track("payment_method_selected", mapOf("type" to paymentMethod.paymentMethodType))
                            scope.onPaymentMethodSelected(paymentMethod.paymentMethodType)
                        }
                    )
                }
            }
        }
        else -> {
            // Use default implementation for loading and error states
            DefaultPaymentMethodSelectionScreen()
        }
    }
}

@Composable
fun PaymentMethodCardWithAnalytics(
    paymentMethod: PrimerComposablePaymentMethod,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PaymentMethodIcon(
                paymentMethodType = paymentMethod.paymentMethodType,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = paymentMethod.paymentMethodName ?: paymentMethod.paymentMethodType,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Select"
            )
        }
    }
}
```

### Conditional Payment Method Filtering

```kotlin
@Composable
fun ConditionalPaymentMethodSelection() {
    val checkoutScope = Primer.checkout()
    val paymentMethodScope = checkoutScope.paymentMethodSelection
    val state by paymentMethodScope.state.collectAsState()
    
    paymentMethodScope.screen = {
        when (state) {
            is PrimerPaymentMethodSelectionScope.State.Ready -> {
                // Filter payment methods based on custom logic
                val filteredPaymentMethods = state.paymentMethods.filter { paymentMethod ->
                    when {
                        // Hide Google Pay if not available
                        paymentMethod.paymentMethodType == "GOOGLE_PAY" && !isGooglePayAvailable() -> false
                        // Hide PayPal for small amounts
                        paymentMethod.paymentMethodType == "PAYPAL" && isSmallAmount(state.orderInfo) -> false
                        // Show surcharge warning for expensive methods
                        paymentMethod.surcharge != null && paymentMethod.surcharge.amount > 5.0 -> true
                        else -> true
                    }
                }
                
                FilteredPaymentMethodScreen(
                    paymentMethods = filteredPaymentMethods,
                    orderInfo = state.orderInfo,
                    onPaymentMethodSelected = paymentMethodScope::onPaymentMethodSelected,
                    onCancel = paymentMethodScope::onCancel
                )
            }
            else -> {
                // Use default screen for loading and error states
                DefaultPaymentMethodSelectionScreen()
            }
        }
    }
}

@Composable
fun FilteredPaymentMethodScreen(
    paymentMethods: List<PrimerComposablePaymentMethod>,
    orderInfo: BasicOrderInfo?,
    onPaymentMethodSelected: (String) -> Unit,
    onCancel: () -> Unit
) {
    // Custom implementation for filtered payment methods
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Available Payment Methods",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(paymentMethods) { paymentMethod ->
            PaymentMethodCardWithWarnings(
                paymentMethod = paymentMethod,
                onSelect = { onPaymentMethodSelected(paymentMethod.paymentMethodType) }
            )
        }
    }
}

@Composable
fun PaymentMethodCardWithWarnings(
    paymentMethod: PrimerComposablePaymentMethod,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Standard payment method display
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                PaymentMethodIcon(
                    paymentMethodType = paymentMethod.paymentMethodType,
                    modifier = Modifier.size(32.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = paymentMethod.paymentMethodName ?: paymentMethod.paymentMethodType,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Show surcharge warning if applicable
            paymentMethod.surcharge?.let { surcharge ->
                if (surcharge.amount > 2.0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Additional fee: ${formatCurrency(surcharge.amount, surcharge.currency)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

fun isGooglePayAvailable(): Boolean {
    // Implementation to check Google Pay availability
    return true // Placeholder
}

fun isSmallAmount(orderInfo: BasicOrderInfo?): Boolean {
    // Implementation to check if order amount is small
    return false // Placeholder
}
```

## Best Practices

### Error Handling and Retry Logic

```kotlin
@Composable
fun RobustPaymentMethodSelection() {
    val checkoutScope = Primer.checkout()
    val paymentMethodScope = checkoutScope.paymentMethodSelection
    
    // Custom screen with comprehensive error handling
    paymentMethodScope.screen = {
        val state by paymentMethodScope.state.collectAsState()
        
        when (state) {
            is PrimerPaymentMethodSelectionScope.State.Loading -> {
                LoadingStateWithTimeout(
                    onTimeout = { /* Handle timeout */ }
                )
            }
            is PrimerPaymentMethodSelectionScope.State.Ready -> {
                if (state.paymentMethods.isEmpty()) {
                    NoPaymentMethodsState(onCancel = paymentMethodScope::onCancel)
                } else {
                    PaymentMethodContent(state, paymentMethodScope)
                }
            }
            is PrimerPaymentMethodSelectionScope.State.Error -> {
                ErrorStateWithRetry(
                    error = state.exception,
                    onRetry = { /* Retry logic */ },
                    onCancel = paymentMethodScope::onCancel
                )
            }
        }
    }
}

@Composable
fun LoadingStateWithTimeout(
    onTimeout: () -> Unit
) {
    val timeoutDuration = 30_000L // 30 seconds
    
    LaunchedEffect(Unit) {
        delay(timeoutDuration)
        onTimeout()
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading payment methods...")
        }
    }
}

@Composable
fun NoPaymentMethodsState(onCancel: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.CreditCardOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No payment methods available",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Please contact support for assistance",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onCancel) {
                Text("Go Back")
            }
        }
    }
}

@Composable
fun ErrorStateWithRetry(
    error: Throwable,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Failed to load payment methods",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error.message ?: "Unknown error occurred",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = onCancel) {
                    Text("Cancel")
                }
                Button(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}
```

### Accessibility Support

```kotlin
@Composable
fun AccessiblePaymentMethodCard(
    paymentMethod: PrimerComposablePaymentMethod,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClickLabel = "Select ${paymentMethod.paymentMethodName}",
                role = Role.Button
            ) { onSelect() }
            .semantics {
                contentDescription = buildString {
                    append("Payment method: ${paymentMethod.paymentMethodName}")
                    paymentMethod.surcharge?.let { surcharge ->
                        append(", Additional fee: ${formatCurrency(surcharge.amount, surcharge.currency)}")
                    }
                }
                stateDescription = "Selectable"
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PaymentMethodIcon(
                paymentMethodType = paymentMethod.paymentMethodType,
                modifier = Modifier
                    .size(40.dp)
                    .semantics { contentDescription = "${paymentMethod.paymentMethodType} icon" }
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = paymentMethod.paymentMethodName ?: paymentMethod.paymentMethodType,
                    style = MaterialTheme.typography.titleMedium
                )
                
                paymentMethod.surcharge?.let { surcharge ->
                    Text(
                        text = "Additional fee: ${formatCurrency(surcharge.amount, surcharge.currency)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
```

### Performance Optimization

```kotlin
@Composable
fun OptimizedPaymentMethodList(
    paymentMethods: List<PrimerComposablePaymentMethod>,
    onPaymentMethodSelected: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = paymentMethods,
            key = { paymentMethod -> paymentMethod.paymentMethodType } // Stable key for performance
        ) { paymentMethod ->
            // Stable lambda for performance
            val onSelect = remember(paymentMethod) {
                { onPaymentMethodSelected(paymentMethod.paymentMethodType) }
            }
            
            PaymentMethodCard(
                paymentMethod = paymentMethod,
                onSelect = onSelect
            )
        }
    }
}
```

## Summary

`PrimerPaymentMethodSelectionScope` provides comprehensive payment method selection capabilities with multiple levels of customization:

### Customization Levels:
- **Default**: Zero configuration, fully functional payment method selection
- **Component-Level**: Customize payment method cards while keeping the default screen structure
- **Screen-Level**: Complete control over the entire payment method selection experience
- **Hybrid**: Mix default components with custom ones for targeted customization

### Key Benefits:
- **Comprehensive**: Handles payment method discovery, order information display, and selection
- **Flexible**: Multiple customization options without breaking functionality
- **Robust**: Built-in error handling, loading states, and retry mechanisms
- **Accessible**: Comprehensive accessibility support with customization options
- **Integrated**: Seamless integration with checkout flow and navigation system
- **Business-Aware**: Supports surcharge display, payment method filtering, and order information

The scope architecture allows you to start with the default implementation and progressively enhance it with custom UI components while maintaining all the business logic, state management, and navigation coordination provided by the SDK.
