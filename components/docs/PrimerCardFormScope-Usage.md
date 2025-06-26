# PrimerCardFormScope Usage Guide

This guide provides comprehensive examples of how to use `PrimerCardFormScope` to create custom card payment forms with complete UI customization and business logic control.

## Overview

`PrimerCardFormScope` provides granular control over card form functionality, offering:

- **State Management**: Observe card form state changes and field validation
- **UI Customization**: Customize every input field and component at any granularity level
- **Component Override**: Mix and match between default and custom components
- **Business Logic**: Handle field updates, validation, and form submission
- **Nested Functionality**: Access country selection and card network selection

### Customization Levels

You can customize at multiple levels:
- **Screen Level**: Override the entire card form screen layout
- **Section Level**: Override card details or billing address sections  
- **Component Level**: Override individual input fields (card number, CVV, expiry, etc.)
- **Hybrid Approach**: Mix default and custom components within the same form

### How Customization Works

**Important**: PrimerCardFormScope works through **property reassignment**, not method overriding:

1. **Default Implementation**: Each composable property has a default implementation that handles state binding and validation automatically

2. **Property Reassignment**: You customize by **reassigning** the var properties:
   ```kotlin
   // This replaces the entire function reference
   scope.cardNumberInput = { modifier -> MyCustomInput(modifier) }
   ```

3. **Usage Patterns**:
   - **Component-Level**: Replace individual input properties while keeping others default
   - **Section-Level**: Replace grouped sections (cardDetails, billingAddress) 
   - **Screen-Level**: Replace the entire screen property for complete control

4. **State & Validation**: Custom components can access form state and validation through `scope.state.collectAsState()`

## Basic Usage

### 1. Basic Usage - Default Components

```kotlin
@Composable
fun BasicCardFormUsage() {
    PrimerCheckout { checkoutScope ->
        // Access the card form scope
        val cardFormScope = checkoutScope.cardForm
        val cardFormState by cardFormScope.state.collectAsState()
        
        // Initialize the form
        LaunchedEffect(Unit) {
            cardFormScope.init()
        }
        
        // Observe form state for business logic
        LaunchedEffect(cardFormState) {
            when {
                cardFormState.isLoading -> {
                    Log.d("CardForm", "Form is loading")
                }
                cardFormState.fieldErrors?.isNotEmpty() == true -> {
                    cardFormState.fieldErrors?.forEach { error ->
                        Log.w("CardForm", "Validation error: ${error.message}")
                    }
                }
            }
        }
        
        // All composable properties use their default implementations
        // SDK automatically displays the form through built-in navigation
    }
}
```

### 2. Component-Level Customization

```kotlin
@Composable
fun ComponentLevelCustomization() {
    PrimerCheckout { checkoutScope ->
        // Access the card form scope and customize specific components
        with(checkoutScope.cardForm) {
            // Override just the submit button with custom styling
            submitButton = { modifier, text ->
                Button(
                    onClick = { onSubmit() },
                    modifier = modifier,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6366F1)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text, fontWeight = FontWeight.Bold)
                }
            }
            
            // Override just the CVV input with show/hide toggle
            cvvInput = { modifier ->
                var cvvVisible by remember { mutableStateOf(false) }
                val cardFormState by state.collectAsState()
                val cvv = cardFormState.data[PrimerInputElementType.CVV] ?: ""
                
                OutlinedTextField(
                    value = cvv,
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
                                imageVector = if (cvvVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (cvvVisible) "Hide CVV" else "Show CVV"
                            )
                        }
                    }
                )
            }
            
            // All other components (card number, expiry, cardholder name, etc.) 
            // remain as default Primer components
            // The SDK automatically displays the form with your customizations
        }
    }
}
```

### 3. Complete Custom Card Form Layout (Manual Navigation)

```kotlin
@Composable
fun FullyCustomCardForm(scope: PrimerCardFormScope) {
    val cardFormState by scope.state.collectAsState()
    
    // Custom screen override
    scope.screen = {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Custom header
            Text(
                text = "Enter Card Details",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Card details section with custom styling
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Card Information",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Use default card details section
                    scope.cardDetails(modifier = Modifier.fillMaxWidth())
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Conditional billing address section
            if (cardFormState.billingFields.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Billing Address",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // Use default billing address section
                        scope.billingAddress(modifier = Modifier.fillMaxWidth())
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Use default submit button
            scope.submitButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Complete Payment"
            )
            
            // Custom error display
            cardFormState.fieldErrors?.forEach { error ->
                Text(
                    text = error.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
```

## Component-Level Customization - Mix and Match

### 3. Individual Field Customization - Override Specific Components

```kotlin
@Composable
fun CustomFieldsCardForm(scope: PrimerCardFormScope) {
    val cardFormState by scope.state.collectAsState()
    
    // Override card number input with enhanced styling and validation
    scope.cardNumberInput = { modifier ->
        val cardNumber = cardFormState.data[PrimerInputElementType.CARD_NUMBER] ?: ""
        
        OutlinedTextField(
            value = cardNumber,
            onValueChange = { newValue ->
                // Apply formatting and update
                val formatted = formatCardNumber(newValue)
                scope.updateCardNumber(formatted)
            },
            modifier = modifier,
            label = { Text("Card Number") },
            placeholder = { Text("1234 5678 9012 3456") },
            leadingIcon = {
                Icon(
                    painter = painterResource(getCardIcon(cardFormState.selectedNetwork)),
                    contentDescription = "Card Type",
                    modifier = Modifier.size(24.dp)
                )
            },
            trailingIcon = {
                if (cardFormState.availableNetworks.size > 1) {
                    IconButton(onClick = { scope.selectCardNetwork(selectAlternateNetwork()) }) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Switch Network")
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
    
    // Override CVV input with show/hide toggle
    scope.cvvInput = { modifier ->
        var cvvVisible by remember { mutableStateOf(false) }
        val cvv = cardFormState.data[PrimerInputElementType.CVV] ?: ""
        
        OutlinedTextField(
            value = cvv,
            onValueChange = { newValue ->
                if (newValue.length <= 4) {
                    scope.updateCvv(newValue)
                }
            },
            modifier = modifier,
            label = { Text("CVV") },
            placeholder = { Text("123") },
            visualTransformation = if (cvvVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(onClick = { cvvVisible = !cvvVisible }) {
                    Icon(
                        imageVector = if (cvvVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (cvvVisible) "Hide CVV" else "Show CVV"
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp)
        )
    }
    
    // Override expiry date input with date picker
    scope.expiryDateInput = { modifier ->
        var showDatePicker by remember { mutableStateOf(false) }
        val expiryDate = cardFormState.data[PrimerInputElementType.EXPIRY_DATE] ?: ""
        
        OutlinedTextField(
            value = expiryDate,
            onValueChange = { newValue ->
                val formatted = formatExpiryDate(newValue)
                scope.updateExpiryDate(formatted)
            },
            modifier = modifier.clickable { showDatePicker = true },
            label = { Text("Expiry Date") },
            placeholder = { Text("MM/YY") },
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.Calendar, contentDescription = "Pick Date")
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp)
        )
        
        if (showDatePicker) {
            ExpiryDatePickerDialog(
                onDateSelected = { month, year ->
                    val formatted = "${month.toString().padStart(2, '0')}/${year.toString().takeLast(2)}"
                    scope.updateExpiryDate(formatted)
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }
    }
    
    // Keep cardholder name as default (not overridden)
    // scope.cardholderNameInput remains the default Primer component
    
    // When using default navigation, you don't need to call scope.screen()
    // The SDK will automatically display the form with your custom components
}
```

### 4. Selective Section Customization

```kotlin
@Composable
fun CustomSectionsCardForm(scope: PrimerCardFormScope) {
    val cardFormState by scope.state.collectAsState()
    
    // Custom card details section with enhanced layout
    scope.cardDetails = { modifier ->
        Card(
            modifier = modifier,
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with card network selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Card Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Custom card network selector
                    scope.cardNetwork(modifier = Modifier)
                }
                
                // Card number with enhanced styling
                scope.cardNumberInput(modifier = Modifier.fillMaxWidth())
                
                // Row layout for CVV and Expiry
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    scope.expiryDateInput(modifier = Modifier.weight(1f))
                    scope.cvvInput(modifier = Modifier.weight(1f))
                }
                
                // Cardholder name
                scope.cardholderNameInput(modifier = Modifier.fillMaxWidth())
            }
        }
    }
    
    // Keep billing address section as default
    // scope.billingAddress remains unchanged
    
    // When using default navigation, the SDK automatically handles screen display
    // No need to call scope.screen() manually
}
```

### 5. Conditional Component Override

```kotlin
@Composable
fun ConditionalCardFormCustomization(
    scope: PrimerCardFormScope,
    isBusinessAccount: Boolean,
    showAdvancedFeatures: Boolean
) {
    val cardFormState by scope.state.collectAsState()
    
    // Business accounts get enhanced submit button
    if (isBusinessAccount) {
        scope.submitButton = { modifier, text ->
            Button(
                onClick = { scope.onSubmit() },
                modifier = modifier.height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1976D2) // Business blue
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    Icons.Default.Business,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$text (Business)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (cardFormState.isLoading) {
                    Spacer(modifier = Modifier.width(8.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White
                    )
                }
            }
        }
    }
    
    // Advanced features for premium users
    if (showAdvancedFeatures) {
        // Enhanced card number input with validation indicators
        scope.cardNumberInput = { modifier ->
            val cardNumber = cardFormState.data[PrimerInputElementType.CARD_NUMBER] ?: ""
            val isValid = remember(cardNumber) { validateCardNumber(cardNumber) }
            
            OutlinedTextField(
                value = cardNumber,
                onValueChange = { newValue ->
                    val formatted = formatCardNumber(newValue)
                    scope.updateCardNumber(formatted)
                },
                modifier = modifier,
                label = { Text("Card Number") },
                supportingText = {
                    Text(
                        text = when {
                            cardNumber.isEmpty() -> "Enter your card number"
                            isValid -> "✓ Valid card number"
                            else -> "Invalid card number"
                        },
                        color = when {
                            cardNumber.isEmpty() -> MaterialTheme.colorScheme.onSurfaceVariant
                            isValid -> Color.Green
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                },
                trailingIcon = {
                    when {
                        cardNumber.isEmpty() -> null
                        isValid -> Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Valid",
                            tint = Color.Green
                        )
                        else -> Icon(
                            Icons.Default.Error,
                            contentDescription = "Invalid",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    }
    
    // When using default navigation, the SDK automatically handles screen display
    // No need to call scope.screen() manually
}
```

## Advanced Usage Patterns

### 6. Form State Management with Custom Logic

```kotlin
@Composable
fun StateDrivenCardForm(scope: PrimerCardFormScope) {
    val cardFormState by scope.state.collectAsState()
    var showAdvancedOptions by remember { mutableStateOf(false) }
    
    // Custom business logic based on form state
    LaunchedEffect(cardFormState.selectedNetwork) {
        // Auto-expand options for certain card types
        showAdvancedOptions = cardFormState.selectedNetwork == CardNetwork.Type.AMERICAN_EXPRESS
    }
    
    LaunchedEffect(cardFormState.fieldErrors) {
        // Custom error handling
        cardFormState.fieldErrors?.forEach { error ->
            when (error.field) {
                "cardNumber" -> showCardNumberError(error.message)
                "cvv" -> showCvvError(error.message)
                else -> showGenericError(error.message)
            }
        }
    }
    
    // Conditional field display based on state
    scope.screen = {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Always show card details
            scope.cardDetails(modifier = Modifier.fillMaxWidth())
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Show advanced options for Amex cards
            if (showAdvancedOptions) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "American Express Additional Options",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Additional Amex-specific fields
                        scope.addressLine1Input(modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        scope.postalCodeInput(modifier = Modifier.fillMaxWidth())
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Show billing address if required
            if (cardFormState.billingFields.isNotEmpty()) {
                scope.billingAddress(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Submit button with dynamic text
            scope.submitButton(
                modifier = Modifier.fillMaxWidth(),
                text = when {
                    cardFormState.isLoading -> "Processing..."
                    cardFormState.selectedNetwork == CardNetwork.Type.AMERICAN_EXPRESS -> "Pay with Amex"
                    else -> "Complete Payment"
                }
            )
        }
    }
}
```

### 7. Multi-Step Form with Navigation

```kotlin
@Composable
fun MultiStepCardForm(scope: PrimerCardFormScope) {
    var currentStep by remember { mutableStateOf(0) }
    val cardFormState by scope.state.collectAsState()
    
    // Custom screen with step navigation
    scope.screen = {
        Column(modifier = Modifier.fillMaxSize()) {
            // Progress indicator
            LinearProgressIndicator(
                progress = (currentStep + 1) / 3f,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when (currentStep) {
                0 -> {
                    // Step 1: Card Details
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Step 1: Card Information",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        scope.cardDetails(modifier = Modifier.fillMaxWidth())
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = { currentStep = 1 },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = isCardDetailsValid(cardFormState)
                        ) {
                            Text("Next: Billing Address")
                        }
                    }
                }
                
                1 -> {
                    // Step 2: Billing Address
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Step 2: Billing Address",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        scope.billingAddress(modifier = Modifier.fillMaxWidth())
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { currentStep = 0 },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Back")
                            }
                            
                            Button(
                                onClick = { currentStep = 2 },
                                modifier = Modifier.weight(1f),
                                enabled = isBillingAddressValid(cardFormState)
                            ) {
                                Text("Next: Review")
                            }
                        }
                    }
                }
                
                2 -> {
                    // Step 3: Review & Submit
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Step 3: Review & Submit",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Review card with masked details
                        ReviewCard(cardFormState = cardFormState)
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { currentStep = 1 },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Back")
                            }
                            
                            scope.submitButton(
                                modifier = Modifier.weight(2f),
                                text = "Complete Payment"
                            )
                        }
                    }
                }
            }
        }
    }
}
```

## Best Practices

### 8. Input Validation & Error Handling

```kotlin
@Composable
fun ValidatedCardForm(scope: PrimerCardFormScope) {
    val cardFormState by scope.state.collectAsState()
    
    // Enhanced validation with real-time feedback
    scope.cardNumberInput = { modifier ->
        val cardNumber = cardFormState.data[PrimerInputElementType.CARD_NUMBER] ?: ""
        val validationState = remember(cardNumber) { 
            validateCardNumberRealTime(cardNumber) 
        }
        
        OutlinedTextField(
            value = cardNumber,
            onValueChange = { newValue ->
                val formatted = formatCardNumber(newValue)
                scope.updateCardNumber(formatted)
            },
            modifier = modifier,
            label = { Text("Card Number") },
            supportingText = {
                when (validationState) {
                    is ValidationState.Empty -> Text("Enter card number")
                    is ValidationState.Invalid -> Text(
                        text = validationState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                    is ValidationState.Valid -> Text(
                        text = "✓ ${validationState.cardType}",
                        color = Color.Green
                    )
                }
            },
            isError = validationState is ValidationState.Invalid,
            colors = OutlinedTextFieldDefaults.colors(
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error
            )
        )
    }
    
    // Custom submit button with validation check
    scope.submitButton = { modifier, text ->
        val isFormValid = isCardFormValid(cardFormState)
        
        Button(
            onClick = { 
                if (isFormValid) {
                    scope.onSubmit()
                } else {
                    showValidationErrors(cardFormState.fieldErrors)
                }
            },
            modifier = modifier,
            enabled = isFormValid && !cardFormState.isLoading
        ) {
            if (cardFormState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text)
        }
    }
    
    scope.screen()
}
```

### 9. Accessibility & Localization

```kotlin
@Composable
fun AccessibleCardForm(scope: PrimerCardFormScope) {
    val cardFormState by scope.state.collectAsState()
    
    // Accessible card number input
    scope.cardNumberInput = { modifier ->
        OutlinedTextField(
            value = cardFormState.data[PrimerInputElementType.CARD_NUMBER] ?: "",
            onValueChange = { scope.updateCardNumber(it) },
            modifier = modifier.semantics {
                contentDescription = "Card number input field"
                testTag = "card_number_input"
            },
            label = { Text(stringResource(R.string.card_number_label)) },
            placeholder = { Text(stringResource(R.string.card_number_placeholder)) },
            supportingText = {
                Text(
                    text = stringResource(R.string.card_number_help),
                    modifier = Modifier.semantics {
                        contentDescription = "Card number should be 16 digits"
                    }
                )
            }
        )
    }
    
    // Accessible submit button
    scope.submitButton = { modifier, text ->
        Button(
            onClick = { scope.onSubmit() },
            modifier = modifier.semantics {
                contentDescription = "Submit payment form"
                testTag = "submit_button"
                if (cardFormState.isLoading) {
                    stateDescription = "Processing payment"
                }
            },
            enabled = !cardFormState.isLoading
        ) {
            Text(text)
        }
    }
    
    scope.screen()
}
```

## Summary

The `PrimerCardFormScope` provides multiple levels of customization flexibility:

### 1. **Screen-Level Customization**
Override the entire card form screen layout for complete control over the user experience and flow.

### 2. **Section-Level Customization**
Override specific sections (card details, billing address) while keeping others as default:
- Custom card details section with enhanced styling
- Default billing address section
- Mix different section styles within the same form

### 3. **Component-Level Customization**
Override individual input fields while keeping others default. Perfect for:
- **Branding**: Custom styling for specific fields
- **Enhanced UX**: Date pickers, show/hide toggles, real-time validation
- **Business Logic**: Conditional fields based on card type or user status

### 4. **Hybrid Approach**
Mix and match default and custom components:
- Use default screen layout with custom input fields
- Override only the components you need to customize
- Apply conditional overrides based on runtime conditions

### 5. **Advanced Patterns**
- Multi-step forms with navigation
- State-driven conditional logic
- Real-time validation with visual feedback
- Accessibility and localization support

### 6. **State Management & Business Logic**
- Observe form state changes reactively
- Implement custom validation logic
- Handle field updates and form submission
- Integrate with country selection and card network selection

This flexible architecture allows developers to:
- **Start Simple**: Use default components and gradually customize as needed
- **Focus on Brand**: Override specific fields to match design requirements
- **Enhance UX**: Add advanced features like date pickers and validation indicators
- **Maintain Security**: Keep Primer's secure validation and processing while customizing UI
- **Scale Complexity**: From simple overrides to complex multi-step flows

The result is complete control over the card form presentation and user experience while leveraging Primer's secure, PCI-compliant payment processing infrastructure.