# Clean Architecture Implementation

This directory contains a clean architecture implementation of the UI components, demonstrating proper separation of concerns and testability.

## Architecture Overview

The clean architecture follows the dependency rule: **dependencies only point inward**. The inner layers know nothing about the outer layers.

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │   UI Components │  │   ViewModels    │  │  UI States   │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
                                │
┌─────────────────────────────────────────────────────────────┐
│                      Domain Layer                           │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │    Use Cases    │  │    Entities     │  │ Repositories │ │
│  │  (Interactors)  │  │                 │  │ (Interfaces) │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
                                │
┌─────────────────────────────────────────────────────────────┐
│                       Data Layer                            │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │  Repositories   │  │  Data Sources   │  │    Models    │ │
│  │(Implementations)│  │ (Remote/Local)  │  │    (DTOs)    │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## Directory Structure

```
clean/
├── domain/                     # Business logic and rules
│   ├── entities/               # Core business objects
│   │   ├── Card.kt
│   │   ├── Payment.kt
│   │   └── PaymentMethod.kt
│   ├── repositories/           # Data access contracts
│   │   ├── PaymentRepository.kt
│   │   └── PaymentMethodRepository.kt
│   └── usecases/              # Business logic operations
│       ├── ProcessCardPaymentUseCase.kt
│       ├── ValidateCardUseCase.kt
│       └── GetAvailablePaymentMethodsUseCase.kt
├── data/                      # Data access implementations
│   ├── repositories/          # Repository implementations
│   │   ├── PaymentRepositoryImpl.kt
│   │   └── PaymentMethodRepositoryImpl.kt
│   └── datasources/          # Data source interfaces and implementations
│       ├── PaymentRemoteDataSource.kt
│       ├── PaymentLocalDataSource.kt
│       └── impl/
│           ├── PaymentRemoteDataSourceImpl.kt
│           └── PaymentLocalDataSourceImpl.kt
├── presentation/             # UI layer
│   ├── card/                # Card payment feature
│   │   ├── CardUiState.kt
│   │   ├── CardViewModel.kt
│   │   └── CardComponent.kt
│   └── checkout/            # Checkout feature
│       ├── CheckoutUiState.kt
│       ├── CheckoutViewModel.kt
│       └── CheckoutComponent.kt
├── di/                      # Dependency injection
│   └── CleanArchitectureModule.kt
└── examples/               # Usage examples
    └── CleanArchitectureExample.kt
```

## Key Principles

### 1. Dependency Inversion
- High-level modules don't depend on low-level modules
- Both depend on abstractions (interfaces)
- Abstractions don't depend on details

### 2. Single Responsibility
- Each class has one reason to change
- Entities handle business rules
- Use cases handle application-specific business rules
- Repositories handle data access

### 3. Separation of Concerns
- **Domain Layer**: Business logic, independent of frameworks
- **Data Layer**: Data access, API calls, database operations
- **Presentation Layer**: UI logic, state management

## Benefits

### 🧪 **Testability**
- Easy to unit test each layer independently
- Mock dependencies at layer boundaries
- Test business logic without UI or databases

### 🔄 **Maintainability**
- Changes in one layer don't affect others
- Easy to add new features or modify existing ones
- Clear separation makes code easier to understand

### 🔌 **Flexibility**
- Easy to swap implementations (e.g., change from REST to GraphQL)
- Framework independence (can change from Compose to Views)
- Database independence (Room, Realm, etc.)

### 📈 **Scalability**
- Clean boundaries make it easy to add new features
- Multiple developers can work on different layers
- Consistent patterns across the codebase

## Usage Examples

### Basic Card Payment
```kotlin
@Composable
fun MyCardScreen() {
    val cardViewModel: CardViewModel = hiltViewModel()
    
    CardComponent(viewModel = cardViewModel)
}
```

### Checkout Flow
```kotlin
@Composable
fun MyCheckoutScreen() {
    val checkoutViewModel: CheckoutViewModel = hiltViewModel()
    
    CheckoutComponent(
        viewModel = checkoutViewModel,
        onPaymentMethodSelected = { method ->
            // Navigate to specific payment method
        }
    )
}
```

## Testing Strategy

### Domain Layer Tests
```kotlin
class ProcessCardPaymentUseCaseTest {
    @Test
    fun `should validate card before processing`() {
        // Test business logic with mock repository
    }
}
```

### Presentation Layer Tests
```kotlin
class CardViewModelTest {
    @Test
    fun `should update UI state when card number changes`() {
        // Test ViewModel with mock use cases
    }
}
```

### Data Layer Tests
```kotlin
class PaymentRepositoryImplTest {
    @Test
    fun `should cache payment after successful processing`() {
        // Test repository with mock data sources
    }
}
```

## Migration from Current Architecture

1. **Phase 1**: Create clean architecture structure alongside existing code
2. **Phase 2**: Gradually migrate components to use clean architecture
3. **Phase 3**: Remove old architecture when migration is complete

This approach allows for gradual migration without breaking existing functionality.

## Dependencies

This implementation uses:
- **Hilt/Dagger**: Dependency injection
- **Coroutines**: Asynchronous operations
- **StateFlow**: Reactive state management
- **Compose**: UI framework

The clean architecture patterns are framework-agnostic and can be adapted to other technologies.
