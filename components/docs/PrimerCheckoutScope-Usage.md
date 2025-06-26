# PrimerCheckoutScope Usage Guide

This guide provides comprehensive examples of how to use `PrimerCheckoutScope` to create custom checkout flows with complete UI customization and business logic control.

## Overview

`PrimerCheckoutScope` is the main entry point for Primer's checkout functionality, providing:

- **State Management**: Observe checkout state changes
- **UI Customization**: Customize every screen and component at any granularity level
- **Component Override**: Mix and match between default and custom components
- **Business Logic**: Handle payment processing and flow control
- **Nested Scopes**: Access card form and payment method selection functionality

### Customization Levels

You can customize at multiple levels:
- **Screen Level**: Override entire screens (splash, loading, success, error)
- **Component Level**: Override individual components (buttons, inputs, sections)
- **Hybrid Approach**: Mix default and custom components within the same screen

## Basic Usage

### 1. Basic Checkout Implementation

```kotlin
@Composable
fun CustomCheckoutScreen() {
    PrimerCheckout { checkoutScope ->
        // Observe checkout state
        val checkoutState by checkoutScope.state.collectAsState()
        
        // Handle state changes
        when (checkoutState) {
            is PrimerCheckoutScope.State.Initializing -> {
                // Show loading indicator
                CircularProgressIndicator()
            }
            is PrimerCheckoutScope.State.Dismissed -> {
                // Navigate back or close
                onCheckoutDismissed()
            }
            is PrimerCheckoutScope.State.Error -> {
                // Show error message
                Text("Error: ${checkoutState.exception.message}")
            }
        }
    }
}
```

### 2. Complete Custom Flow with UI Customization

```kotlin
@Composable
fun FullyCustomizedCheckout() {
    PrimerCheckout { checkoutScope ->
        // Customize the main container
        checkoutScope.container = { content ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                content()
            }
        }
        
        // Custom splash screen
        checkoutScope.splashScreen = {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_logo),
                    contentDescription = "Logo"
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Initializing Checkout...",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }
        }
        
        // Custom loading screen
        checkoutScope.loadingScreen = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Processing Payment...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        
        // Custom success screen
        checkoutScope.successScreen = {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = Color.Green,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Payment Successful!",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your payment has been processed successfully.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { checkoutScope.onDismiss() }
                ) {
                    Text("Continue")
                }
            }
        }
        
        // Custom error screen
        checkoutScope.errorScreen = { message ->
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Error",
                    tint = Color.Red,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Payment Failed",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row {
                    Button(
                        onClick = { /* Retry logic */ }
                    ) {
                        Text("Retry")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    OutlinedButton(
                        onClick = { checkoutScope.onDismiss() }
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}
```

## Advanced Usage

### 3. State-Driven UI with Business Logic

```kotlin
@Composable
fun StateDrivenCheckout() {
    var showPaymentMethods by remember { mutableStateOf(false) }
    var selectedPaymentMethod by remember { mutableStateOf<String?>(null) }
    
    PrimerCheckout { checkoutScope ->
        val checkoutState by checkoutScope.state.collectAsState()
        
        // Custom business logic based on state
        LaunchedEffect(checkoutState) {
            when (checkoutState) {
                is PrimerCheckoutScope.State.Initializing -> {
                    logAnalyticsEvent("checkout_initialized")
                }
                is PrimerCheckoutScope.State.Dismissed -> {
                    logAnalyticsEvent("checkout_dismissed")
                    navigateToOrderSummary()
                }
                is PrimerCheckoutScope.State.Error -> {
                    logAnalyticsEvent("checkout_error", checkoutState.exception.message)
                    showErrorDialog(checkoutState.exception.message)
                }
            }
        }
        
        // Custom container with state-based styling
        checkoutScope.container = { content ->
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                content()
            }
        }
        
        // Conditional UI based on checkout state
        when (checkoutState) {
            is PrimerCheckoutScope.State.Initializing -> {
                checkoutScope.splashScreen()
            }
            else -> {
                // Show main checkout UI
                if (showPaymentMethods) {
                    PaymentMethodSelection(
                        scope = checkoutScope.paymentMethodSelection,
                        onMethodSelected = { method ->
                            selectedPaymentMethod = method
                            showPaymentMethods = false
                        }
                    )
                } else {
                    // Show card form or other content
                    CardFormScreen(
                        scope = checkoutScope.cardForm,
                        onBack = { showPaymentMethods = true }
                    )
                }
            }
        }
    }
}
```

### 4. Component-Level Customization - Mix and Match

```kotlin
@Composable
fun HybridCardFormScreen(
    scope: PrimerCardFormScope,
    onBack: () -> Unit
) {
    val cardFormState by scope.state.collectAsState()
    
    // Override specific components while keeping others default
    
    // Custom submit button only
    scope.submitButton = { modifier, text ->
        Button(
            onClick = { scope.onSubmit() },
            modifier = modifier,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6366F1) // Custom purple
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (cardFormState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text, fontWeight = FontWeight.Bold)
        }
    }
    
    // Custom card number input with enhanced styling
    scope.cardNumberInput = { modifier ->
        OutlinedTextField(
            value = cardFormState.data[PrimerInputElementType.CARD_NUMBER] ?: "",
            onValueChange = { scope.updateCardNumber(it) },
            modifier = modifier,
            label = { Text("Card Number") },
            placeholder = { Text("1234 5678 9012 3456") },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_credit_card),
                    contentDescription = "Card"
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF6366F1),
                focusedLabelColor = Color(0xFF6366F1)
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}
```

### 5. Granular Component Override Examples

```kotlin
@Composable
fun MixedCustomizationExample() {
    PrimerCheckout { checkoutScope ->
        
        // Customize card form components selectively
        with(checkoutScope.cardForm) {
            // Keep default screen layout
            // Override only the CVV input with custom design
            cvvInput = { modifier ->
                var cvvVisible by remember { mutableStateOf(false) }
                
                OutlinedTextField(
                    value = state.collectAsState().value.data[PrimerInputElementType.CVV] ?: "",
                    onValueChange = { updateCvv(it) },
                    modifier = modifier,
                    label = { Text("CVV") },
                    visualTransformation = if (cvvVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = { cvvVisible = !cvvVisible }) {
                            Icon(
                                imageVector = if (cvvVisible) {
                                    Icons.Default.Visibility
                                } else {
                                    Icons.Default.VisibilityOff
                                },
                                contentDescription = if (cvvVisible) "Hide CVV" else "Show CVV"
                            )
                        }
                    }
                )
            }
            
            // Custom expiry date with date picker
            expiryDateInput = { modifier ->
                var showDatePicker by remember { mutableStateOf(false) }
                
                OutlinedTextField(
                    value = state.collectAsState().value.data[PrimerInputElementType.EXPIRY_DATE] ?: "",
                    onValueChange = { updateExpiryDate(it) },
                    modifier = modifier.clickable { showDatePicker = true },
                    label = { Text("Expiry Date") },
                    placeholder = { Text("MM/YY") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.Calendar, contentDescription = "Pick date")
                        }
                    }
                )
                
                if (showDatePicker) {
                    DatePickerDialog(
                        onDateSelected = { date ->
                            updateExpiryDate(formatDate(date))
                            showDatePicker = false
                        },
                        onDismiss = { showDatePicker = false }
                    )
                }
            }
            
            // Keep all other inputs as default
            // cardNumberInput, cardholderNameInput, etc. remain unchanged
        }
    }
}
```

### 6. Conditional Component Override

```kotlin
@Composable
fun ConditionalCustomization(
    isDarkTheme: Boolean,
    isPremiumUser: Boolean
) {
    PrimerCheckout { checkoutScope ->
        
        with(checkoutScope.cardForm) {
            // Apply different customizations based on conditions
            
            if (isPremiumUser) {
                // Premium users get enhanced UI
                submitButton = { modifier, text ->
                    Button(
                        onClick = { onSubmit() },
                        modifier = modifier,
                        gradient = Brush.linearGradient(
                            colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                        )
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("$text (Premium)")
                    }
                }
            } else {
                // Standard users get default button
                // submitButton remains default
            }
            
            if (isDarkTheme) {
                // Override inputs for dark theme
                cardNumberInput = { modifier ->
                    DarkThemeTextField(
                        value = state.collectAsState().value.data[PrimerInputElementType.CARD_NUMBER] ?: "",
                        onValueChange = { updateCardNumber(it) },
                        modifier = modifier,
                        label = "Card Number"
                    )
                }
            }
            // Light theme uses default inputs
        }
    }
}
```

### 7. Integration with Card Form Scope
```

### 5. Payment Method Selection Integration

```kotlin
@Composable
fun PaymentMethodSelection(
    scope: PrimerPaymentMethodSelectionScope,
    onMethodSelected: (String) -> Unit
) {
    val selectionState by scope.state.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Select Payment Method",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        items(selectionState.availablePaymentMethods) { paymentMethod ->
            PaymentMethodCard(
                paymentMethod = paymentMethod,
                isSelected = selectionState.selectedPaymentMethod == paymentMethod,
                onClick = { 
                    scope.selectPaymentMethod(paymentMethod)
                    onMethodSelected(paymentMethod.id)
                }
            )
        }
    }
}
```

## Best Practices

### 1. Error Handling

```kotlin
@Composable
fun RobustCheckout() {
    PrimerCheckout { checkoutScope ->
        val checkoutState by checkoutScope.state.collectAsState()
        
        // Custom error handling with retry logic
        checkoutScope.errorScreen = { message ->
            ErrorScreenWithRetry(
                message = message,
                onRetry = {
                    // Implement retry logic
                    checkoutScope.cardForm.init()
                },
                onCancel = {
                    checkoutScope.onDismiss()
                }
            )
        }
        
        // Log all state changes for debugging
        LaunchedEffect(checkoutState) {
            Log.d("Checkout", "State changed: $checkoutState")
        }
    }
}
```

### 2. Accessibility

```kotlin
@Composable
fun AccessibleCheckout() {
    PrimerCheckout { checkoutScope ->
        // Add accessibility labels
        checkoutScope.container = { content ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics {
                        contentDescription = "Checkout container"
                    }
            ) {
                content()
            }
        }
        
        // Accessible error screen
        checkoutScope.errorScreen = { message ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics {
                        contentDescription = "Error occurred: $message"
                    }
            ) {
                // Error UI with proper accessibility
                Text(
                    text = "Payment Error",
                    modifier = Modifier.semantics {
                        heading()
                    }
                )
                Text(
                    text = message,
                    modifier = Modifier.semantics {
                        liveRegion = LiveRegionMode.Polite
                    }
                )
            }
        }
    }
}
```

## Summary

The `PrimerCheckoutScope` provides multiple levels of customization flexibility:

### 1. **Screen-Level Customization**
Override entire screens (splash, loading, success, error) for complete control over the user experience.

### 2. **Component-Level Customization**
Override individual components (buttons, inputs, sections) while keeping the rest as default. This is perfect for:
- Branding specific elements (custom buttons, colors)
- Adding enhanced functionality (date pickers, visibility toggles)
- Conditional customization based on user types or themes

### 3. **Hybrid Approach**
Mix and match default and custom components within the same screen:
- Use default screen layout with custom components
- Override only the components you need to customize
- Apply conditional overrides based on runtime conditions

### 4. **State Management**
Use `StateFlow` to react to checkout state changes and implement custom business logic.

### 5. **Business Logic Control**
Call scope functions to control the checkout flow programmatically.

This flexible architecture allows developers to:
- **Start Simple**: Use default components and gradually customize as needed
- **Brand Selectively**: Override only specific components to match brand guidelines  
- **Enhance Functionality**: Add custom features while keeping core payment logic secure
- **Maintain Consistency**: Keep default Primer components where custom design isn't needed

The result is complete control over both presentation and business logic while leveraging Primer's secure, compliant payment processing infrastructure.
