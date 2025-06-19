# Composable API Refactoring Plan

## Goal
Expose composables through nested scopes on `PrimerCheckoutScope` while maintaining existing navigation logic from `CheckoutNavHost`.

## Current Problem
- `showCheckout()` has too many nullable parameters
- Client code needs better structure and organization
- Need cleaner API that maintains navigation and groups related functionality

## Codebase Analysis & Issues Found

### Current Architecture Review
After analyzing the existing codebase, several issues were identified with the original plan:

**Current State:**
- `CheckoutViewModel` already implements `PrimerCheckoutScope`
- `CardViewModel` already implements `CardFormScope` 
- `PaymentMethodSelectionViewModel` already implements `PaymentMethodSelectionScope`
- Scope interfaces use companion object extension functions for composables
- Repository implementations use `*Impl` suffix pattern

**Naming Convention Issues:**
1. **Interface Naming**: Current plan suggested inconsistent "Primer" prefix usage
2. **Implementation Classes**: Suggested `ScopeDefaults` but codebase uses `*Impl` pattern
3. **Companion Objects**: Current scopes use extension functions in companion objects

**Architecture Issues:**
1. **ViewModels already implement scopes**: The inheritance chain `ViewModel → ScopeDefaults → Scope` adds unnecessary complexity
2. **Nested scope initialization**: Original plan had flawed ViewModel lifecycle management
3. **Breaking changes**: Need to remove companion object pattern per user requirements

## User Decisions Integration
- ✅ **Break existing companion object pattern** - move to property-based overrides
- ✅ **Nested scopes as properties** - not separate ViewModels
- ✅ **Clean new API** - no backward compatibility concerns
- ✅ **Primer prefix everywhere** - consistent naming across all scope interfaces

## Proposed Solution: Nested Scopes with Inheritance Architecture

### 1. Define Scope Interfaces with Composable Properties

**File: `PrimerCheckoutScope.kt`**
```kotlin
interface PrimerCheckoutScope {
    val state: StateFlow<State>
    
    // Non-nullable composables with scope receiver
    var Container: @Composable PrimerCheckoutScope.(content: @Composable () -> Unit) -> Unit
    var SplashScreen: @Composable PrimerCheckoutScope.() -> Unit
    var LoadingScreen: @Composable PrimerCheckoutScope.() -> Unit
    var SuccessScreen: @Composable PrimerCheckoutScope.() -> Unit
    var ErrorScreen: @Composable PrimerCheckoutScope.(message: String) -> Unit
    
    // Nested scopes
    val cardFormScope: PrimerCardFormScope
    val paymentSelectionScope: PrimerPaymentMethodSelectionScope
    
    fun onDismiss()
    
    sealed interface State { /* existing */ }
}
```

**File: `PrimerCardFormScope.kt`** (renamed from `CardFormScope.kt`)
```kotlin
interface PrimerCardFormScope {
    val state: StateFlow<State>
    
    // Business methods from current CardFormScope
    fun updateCardNumber(cardNumber: String)
    fun updateCvv(cvv: String)
    fun updateExpiryDate(expiryDate: String)
    fun updateCardholderName(cardholderName: String)
    fun onSubmit()
    fun onBack()
    fun onCancel()
    
    // Non-nullable composable properties (replacing companion object extensions)
    var PrimerCardFormScreen: @Composable () -> Unit
    var PrimerSubmitButton: @Composable (modifier: Modifier, text: String) -> Unit
    var PrimerCardNumberInput: @Composable (modifier: Modifier) -> Unit
    var PrimerCvvInput: @Composable (modifier: Modifier) -> Unit
    var PrimerExpiryDateInput: @Composable (modifier: Modifier) -> Unit
    // ... other input composables
    
    data class State(
        val cardFields: List<PrimerInputElementType> = emptyList(),
        val billingFields: List<PrimerInputElementType> = emptyList(),
        val fieldErrors: List<PrimerInputValidationError> = emptyList(),
        val inputFields: Map<PrimerInputElementType, String> = emptyMap(),
        val isLoading: Boolean = false,
        val isSubmitEnabled: Boolean = false,
    )
}
```

**File: `PrimerPaymentMethodSelectionScope.kt`** (renamed from `PaymentMethodSelectionScope.kt`)
```kotlin
interface PrimerPaymentMethodSelectionScope {
    val state: StateFlow<State>
    
    // Business methods from current PaymentMethodSelectionScope
    fun onPaymentMethodSelected(paymentMethod: PrimerComposablePaymentMethod)
    fun onCancel()
    
    // Non-nullable composable properties (replacing companion object extensions)
    var PrimerPaymentSelectionScreen: @Composable () -> Unit
    var PrimerPaymentMethodCard: @Composable (modifier: Modifier, onPaymentMethodSelected: () -> Unit) -> Unit
    
    sealed interface State {
        data object Loading : State
        data class Ready(
            val paymentMethods: List<PrimerComposablePaymentMethod>,
            val title: String,
        ) : State
        data class Error(val exception: Throwable) : State
    }
}
```

### 2. Create Abstract Implementation Classes with Default Composables

## Implementation Class Naming (Final)

Based on user feedback:

**Selected Pattern: `ScopeDefaults`**
- Clear indication of providing default implementations
- Matches the purpose of these classes
- Consistent with user preference

**Final Naming:**
1. `CheckoutScopeDefaults` - base implementation for `PrimerCheckoutScope`
2. `CardFormScopeDefaults` - base implementation for `PrimerCardFormScope`
3. `PaymentMethodSelectionScopeDefaults` - base implementation for `PrimerPaymentMethodSelectionScope`

**NEW File: `CheckoutScopeDefaults.kt`** (internal)
```kotlin
internal abstract class CheckoutScopeDefaults : PrimerCheckoutScope {
    // Default composables with scope receiver
    override var Container: @Composable PrimerCheckoutScope.(content: @Composable () -> Unit) -> Unit = { content ->
        // Default container implementation
        ModalBottomSheet(onDismissRequest = ::onDismiss) {
            content()
        }
    }
    
    override var SplashScreen: @Composable PrimerCheckoutScope.() -> Unit = {
        DefaultSplashScreen() // Internal default implementation
    }
    
    override var LoadingScreen: @Composable PrimerCheckoutScope.() -> Unit = {
        DefaultLoadingScreen() // Internal default implementation
    }
    
    override var SuccessScreen: @Composable PrimerCheckoutScope.() -> Unit = {
        DefaultSuccessScreen() // Internal default implementation
    }
    
    override var ErrorScreen: @Composable PrimerCheckoutScope.(message: String) -> Unit = { message ->
        DefaultErrorScreen(message) // Internal default implementation
    }
    
    // Nested scopes initialized with viewModel<> from Compose
    @Composable
    override val cardFormScope: PrimerCardFormScope
        get() = viewModel<CardViewModel>()
    
    @Composable
    override val paymentSelectionScope: PrimerPaymentMethodSelectionScope
        get() = viewModel<PaymentMethodSelectionViewModel>()
}
```

**NEW File: `CardFormScopeDefaults.kt`** (internal)
```kotlin
internal abstract class CardFormScopeDefaults : PrimerCardFormScope {
    // Default composable implementations
    override var PrimerCardFormScreen: @Composable () -> Unit = {
        DefaultCardFormScreen() // Internal default implementation
    }
    
    override var PrimerSubmitButton: @Composable (modifier: Modifier, text: String) -> Unit = { modifier, text ->
        DefaultSubmitButton(modifier, text)
    }
    
    override var PrimerCardNumberInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        DefaultCardNumberInput(modifier)
    }
    
    // ... other default composable implementations
}
```

**NEW File: `PaymentMethodSelectionScopeDefaults.kt`** (internal)
```kotlin
internal abstract class PaymentMethodSelectionScopeDefaults : PrimerPaymentMethodSelectionScope {
    // Default composable implementations
    override var PrimerPaymentSelectionScreen: @Composable () -> Unit = {
        DefaultPaymentMethodSelectionScreen() // Internal default implementation
    }
    
    override var PrimerPaymentMethodCard: @Composable (modifier: Modifier, onPaymentMethodSelected: () -> Unit) -> Unit = { modifier, onSelected ->
        DefaultPaymentMethodCard(modifier, onSelected)
    }
}
```

### 3. ViewModels Extend ScopeDefaults Classes

**File: `CheckoutViewModel.kt`** (Extends CheckoutScopeDefaults)
```kotlin
internal class CheckoutViewModel : ViewModel(), CheckoutScopeDefaults(), DISdkComponent {
    // Business logic only - composables inherited from ScopeDefaults
    private val _state = MutableStateFlow<PrimerCheckoutScope.State>(PrimerCheckoutScope.State.Initializing)
    override val state: StateFlow<PrimerCheckoutScope.State> = _state.asStateFlow()
    
    // Nested scopes initialized via @Composable properties in CheckoutScopeDefaults
    // No initialization needed here - handled by ScopeDefaults base class
    
    fun initialize(context: Context, clientToken: String, primerSettings: PrimerSettings) {
        // Existing initialization logic
    }
    
    override fun onDismiss() {
        // Existing dismiss logic
    }
}
```

**File: `CardViewModel.kt`** (Keep as ViewModel, extend CardFormScopeDefaults)
```kotlin
internal class CardViewModel : ViewModel(), CardFormScopeDefaults(), DISdkComponent {
    // Existing business logic preserved
    private val _uiState = MutableStateFlow<PrimerCardFormScope.State>(PrimerCardFormScope.State())
    override val state: StateFlow<PrimerCardFormScope.State> = _uiState.asStateFlow()
    
    // All existing update methods and business logic remain the same
    override fun updateCardNumber(cardNumber: String) {
        setDataInteractor.updateInput(cardNumber, PrimerInputElementType.CARD_NUMBER)
    }
    
    override fun onSubmit() {
        // Existing business logic
    }
    
    // Composables inherited from CardFormScopeDefaults
}
```

**File: `PaymentMethodSelectionViewModel.kt`** (Keep as ViewModel, extend PaymentMethodSelectionScopeDefaults)
```kotlin
internal class PaymentMethodSelectionViewModel : ViewModel(), PaymentMethodSelectionScopeDefaults(), DISdkComponent {
    // Existing business logic preserved
    private val _uiState = MutableStateFlow<PaymentMethodSelectionScope.State>(PaymentMethodSelectionScope.State.Loading)
    override val state: StateFlow<PaymentMethodSelectionScope.State> = _uiState.asStateFlow()
    
    // All existing methods remain the same
    override fun onPaymentMethodSelected(paymentMethod: PrimerComposablePaymentMethod) {
        // Existing business logic
    }
    
    override fun onCancel() {
        // Existing business logic
    }
    
    // Composables inherited from PaymentMethodSelectionScopeDefaults
}
```

### 4. Client Usage Example - Custom Content with Scope Capture

**File: `ComposableCheckoutFragment.kt`**
```kotlin
with(Primer) {
    configure(token)
    val scope = showCheckout()
    
    // Override container and main screens (these have scope receivers)
    scope.Container = { content ->
        // Custom container implementation - 'this' is PrimerCheckoutScope
        MyCustomContainer {
            content() // Contains navigation
        }
    }
    
    scope.SplashScreen = {
        // Custom splash screen - 'this' is PrimerCheckoutScope
        MyCustomSplashScreen {
            Button(onClick = ::onDismiss) { Text("Close") }
        }
    }
    
    // Override nested scope screens using 'let' to capture scope
    scope.cardFormScope.let { cardScope ->
        cardScope.PrimerCardFormScreen = {
            // Custom card form - capture scope to access its methods
            MyCustomCardForm(
                onSubmit = { cardScope.submit() }, // Access via captured scope
                isValid = cardScope.isValid.collectAsState().value
            )
        }
    }
    
    scope.someOtherScope.let { otherScope ->
        otherScope.SomeOtherScreen = {
            // Custom screen - capture scope to access its methods
            MyCustomScreen {
                Button(onClick = { otherScope.foo() }) { // Access via captured scope
                    Text("Call Foo")
                }
            }
        }
    }
}
```

### Key Benefits of Scope Capture Pattern

1. **Explicit Scope Access**: Use `let` to capture scope and call methods via `it.submit()`, `it.foo()`
2. **State Access**: Access to all StateFlow properties like `it.isValid`, `it.cardData`
3. **Type Safety**: IDE knows which methods are available on captured scope
4. **Complete Override**: Replace entire screen implementations while keeping business logic
5. **Clear Intent**: The `let` pattern makes it obvious that you're accessing the scope

## Benefits of Nested Scopes

1. **Better Organization**: Related functionality grouped under specific scopes
2. **Cleaner API**: Main screens as properties, specialized screens under sub-scopes
3. **Extensibility**: Easy to add new payment method scopes (bankScope, walletScope, etc.)
4. **Type Safety**: Each scope has its own interface with relevant methods
5. **Discoverability**: IDE autocomplete shows available scopes and their methods

## Corrected Implementation Steps (9 Steps)

### Step 0: Create Experimental Branch
- Create new branch `experimental_public_api` from current branch
- Commit the plan to this branch first
- Each subsequent step will be committed atomically
- This allows for easy rollback and incremental review

### Step 1: Rename Existing Screen Components (4-6 files)
- Rename existing screens to `DefaultSplashScreen.kt`, `DefaultLoadingScreen.kt`, `DefaultSuccessScreen.kt`, `DefaultErrorScreen.kt`
- Rename existing `CardFormScreen.kt` → `DefaultCardFormScreen.kt`, `PaymentMethodSelectionScreen.kt` → `DefaultPaymentMethodSelectionScreen.kt`
- Rename @Composable functions to match new naming pattern (e.g., `SplashScreen()` → `DefaultSplashScreen()`)
- These will be used by ScopeDefaults classes

### Step 2: Update Scope Interfaces - Breaking Changes (3 files)
- Update `PrimerCheckoutScope.kt` - add composable properties, **remove companion object**
- Rename `CardFormScope.kt` → `PrimerCardFormScope.kt` - add composable properties, **remove companion object**
- Rename `PaymentMethodSelectionScope.kt` → `PrimerPaymentMethodSelectionScope.kt` - add composable properties, **remove companion object**

### Step 3: Create ScopeDefaults Classes with Lazy Initialization (3 files)
- Create `CheckoutScopeDefaults.kt` - provide default composable implementations + lazy nested scope initialization
- Create `CardFormScopeDefaults.kt` - provide default composable implementations
- Create `PaymentMethodSelectionScopeDefaults.kt` - provide default composable implementations

### Step 4: Update ViewModels to Extend ScopeDefaults (3 files)
- Update `CheckoutViewModel.kt` - extend `CheckoutScopeDefaults`, remove manual nested scope initialization
- Update `CardViewModel.kt` - extend `CardFormScopeDefaults`, preserve existing business logic
- Update `PaymentMethodSelectionViewModel.kt` - extend `PaymentMethodSelectionScoapeDefaults`, preserve existing business logic

### Step 5: Update Internal Screens to Remove Extension Usage (6-8 files)
- Update all internal screens that use companion object extensions
- Replace `scope.PrimerSubmitButton()` with direct ViewModel access
- Update imports and references

### Step 6: Update Navigation Integration (2 files)
- Update `CheckoutNavHost.kt` - use scope properties instead of nullable parameters
- Update `Checkout.kt` - integrate with new scope-based navigation

### Step 7: Update Primer Public API (2 files)
- Update `Primer.kt` - remove nullable parameters from `showCheckout()`, return scope for property-based customization
- **Breaking change** - no backward compatibility needed

### Step 8: Update Example App (2-3 files)
- Update example app to demonstrate new property-based API
- Show nested scope customization patterns
- Remove old extension function usage

### Step 9: Code Review and Final Integration
- Compare `experimental_public_api` branch with `u/dp/clean_architecture` branch
- Code review based on differences between the two branches
- Ensure no compilation errors or runtime issues

## Navigation Integration

### How ViewModels Connect with Navigation

**Update CheckoutNavHost.kt:**
```kotlin
@Composable
internal fun PrimerCheckoutScope.CheckoutNavHost(
    modifier: Modifier = Modifier,
) {
    // Use properties from scope if available, otherwise use defaults
    val navController = LocalNavController.current
    
    NavHost(
        navController = navController,
        startDestination = Screen.PaymentsList.route,
        modifier = modifier,
    ) {
        composable(Screen.Splash.route) {
            SplashScreen ?: SplashScreen()
        }
        
        composable(Screen.Loading.route) {
            LoadingScreen ?: LoadingScreen()
        }
        
        composable(Screen.PaymentsList.route) {
            // Access nested scope's ViewModel
            paymentSelectionScope.run {
                PaymentSelectionScreen()
            }
        }
        
        composable(Screen.CardForm.route) {
            // Access nested scope's ViewModel
            cardFormScope.run {
                CardFormScreen()
            }
        }
        
        composable(Screen.Success.route) {
            SuccessScreen ?: SuccessScreen()
        }
        
        composable(Screen.Error.route) { backStackEntry ->
            val error = backStackEntry.savedStateHandle.get<String>("error")
            error?.let { 
                ErrorScreen?.invoke(it) ?: ErrorScreen()
            }
        }
    }
}
```

### ViewModel Lifecycle Management

The ViewModels are managed hierarchically:
- `CheckoutViewModel` (parent) - owns the checkout session
- `CardViewModel`, `PaymentMethodSelectionViewModel` (children) - scoped to their screens

```kotlin
// In CheckoutViewModel
override val cardFormScope: CardFormScope by lazy {
    // This creates a child ViewModel scoped to the checkout session
    ViewModelProvider(
        viewModelStoreOwner, // Same owner as CheckoutViewModel
        factory // Custom factory that injects dependencies
    ).get(CardViewModel::class.java)
}
```

### Container Usage with Navigation

The `Container` property allows custom containers while preserving navigation:

```kotlin
scope.Container = { content ->
    // This lambda receives the navigation-enabled content
    MyCustomContainer {
        content() // This includes CheckoutNavHost with all navigation logic
    }
}
```

If no custom container is provided, the default ModalBottomSheet is used (as in current implementation).

## Architecture Summary

### Corrected Inheritance Chain: ViewModel → ScopeDefaults → Scope

1. **Scope Interfaces** - Define the public API
   - State properties and business methods
   - Composable property signatures
   - Nested scope references

2. **ScopeDefaults Classes** - Provide default composables
   - Implement default UI components
   - Properties are mutable so clients can override
   - Handle nested scope initialization with `viewModel<>()`
   - No business logic

3. **ViewModels** - Extend ScopeDefaults, handle business logic
   - Inherit default composables from ScopeDefaults
   - Implement state management and business logic
   - Can be returned directly as Scope instances

### Why This Architecture?

- **Single Responsibility**: ViewModels handle logic, ScopeDefaults handles UI defaults
- **No @Composable in ViewModels**: Composables are in ScopeDefaults, keeping ViewModels clean
- **Flexibility**: Clients can override any composable property
- **Type Safety**: Everything is strongly typed with scope receivers
- **No nullable properties**: All composables have default implementations
- **Breaking Change Justified**: Removes companion object pattern for cleaner property-based API
- **Consistent Naming**: "Primer" prefix on all scope interfaces
- **Compose Integration**: Nested scopes use `viewModel<>()` from Compose for proper lifecycle

# Critical Issues Found & Resolved

## Issue 1: Nested Scope Initialization

The original plan had a problem in `CheckoutViewModel`:
```kotlin
override val cardFormScope: PrimerCardFormScope by lazy {
    ViewModelProvider(viewModelStoreOwner).get(CardViewModel::class.java) // ❌ Won't work
}
```

**Problem**: `CheckoutViewModel` is a ViewModel itself and doesn't have access to `ViewModelStoreOwner`.

## Issue 2: Current ViewModels Already Implement Scopes

Analysis revealed:
- `CheckoutViewModel` already implements `PrimerCheckoutScope`
- `CardViewModel` already implements `CardFormScope`
- `PaymentMethodSelectionViewModel` already implements `PaymentMethodSelectionScope`

**Problem**: The inheritance chain `ViewModel → ScopeDefaults → Scope` adds unnecessary complexity.

## Issue 3: Companion Object Pattern

Current scopes use companion object extension functions:
```kotlin
companion object {
    @Composable
    fun CardFormScope.PrimerSubmitButton(modifier: Modifier = Modifier) {
        SubmitButton(modifier = modifier)
    }
}
```

**Problem**: User wants to break this pattern for property-based overrides.

## Corrected Approach

Use `viewModel<>()` in ScopeDefaults:

```kotlin
internal abstract class CheckoutScopeDefaults : PrimerCheckoutScope {
    // Nested scopes initialized with viewModel<> from Compose
    @Composable
    override val cardFormScope: PrimerCardFormScope
        get() = viewModel<CardViewModel>()
    
    @Composable
    override val paymentSelectionScope: PrimerPaymentMethodSelectionScope
        get() = viewModel<PaymentMethodSelectionViewModel>()
}

internal class CheckoutViewModel : ViewModel(), CheckoutScopeDefaults(), DISdkComponent {
    // Business logic only - nested scopes handled by ScopeDefaults
}
```

## Final API Usage Pattern

```kotlin
with(Primer) {
    configure(token)
    val scope = showCheckout()
    
    // Override main scope screens
    scope.Container = { content ->
        MyCustomContainer { content() }
    }
    
    scope.SplashScreen = {
        MyCustomSplashScreen()
    }
    
    // Override nested scope screens using property access
    scope.cardFormScope.PrimerCardFormScreen = {
        MyCustomCardForm(
            onSubmit = { scope.cardFormScope.onSubmit() },
            isValid = scope.cardFormScope.state.collectAsState().value.isSubmitEnabled
        )
    }
    
    scope.paymentMethodSelectionScope.PrimerPaymentSelectionScreen = {
        MyCustomPaymentSelection(
            onSelect = { method -> scope.paymentMethodSelectionScope.onPaymentMethodSelected(method) }
        )
    }
    
    // Override individual input components
    scope.cardFormScope.PrimerCardNumberInput = { modifier ->
        MyCustomCardNumberInput(
            modifier = modifier,
            onValueChange = { scope.cardFormScope.updateCardNumber(it) }
        )
    }
}
```
