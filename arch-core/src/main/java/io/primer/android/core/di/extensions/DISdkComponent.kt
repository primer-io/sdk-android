package io.primer.android.core.di.extensions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer

/**
 * Extension functions for [DISdkComponent] providing convenient dependency resolution methods.
 *
 * ## Dependency Resolution Patterns
 *
 * This file provides two primary patterns for dependency resolution in the SDK:
 *
 * ### 1. `resolve()` - Component-level resolution (this file)
 * Used by classes implementing [DISdkComponent] such as:
 * - Component factories and providers
 * - Public API state classes (PrimerCheckoutScope, CardFormState, etc.)
 * - ViewModels and UI components
 *
 * ```kotlin
 * class PaymentComponent : DISdkComponent {
 *     companion object : DISdkComponent {
 *         fun create(): PaymentComponent = PaymentComponent(
 *             tokenizationDelegate = resolve(),    // Resolves from DI graph
 *             paymentDelegate = resolve()          // Extension resolves automatically
 *         )
 *     }
 * }
 * ```
 *
 * ### 2. `sdk().resolve()` - Container-level resolution (in DependencyContainer)
 * Used within [DependencyContainer] implementations to access global SDK dependencies:
 *
 * ```kotlin
 * class PaymentMethodContainer(
 *     private val sdk: () -> SdkContainer,
 * ) : DependencyContainer() {
 *     override fun registerInitialDependencies() {
 *         registerFactory {
 *             PaymentService(
 *                 httpClient = sdk().resolve(),       // Global HTTP client
 *                 analytics = sdk().resolve(),        // Global analytics
 *                 settings = sdk().resolve(),         // Global settings
 *                 localMapper = resolve()             // Local to this container
 *             )
 *         }
 *     }
 * }
 * ```
 *
 * ## Resolution Chain
 * - `resolve()` delegates to `getSdkContainer().resolve()`
 * - `sdk().resolve()` directly accesses the global SDK container
 * - Both patterns provide type-safe dependency resolution with compile-time checking
 *
 * ## Usage Guidelines
 * - Use `resolve()` in [DISdkComponent] implementations for clean, extension-based resolution
 * - Use `sdk().resolve()` in [DependencyContainer] subclasses for explicit SDK dependency access
 * - Both patterns support named resolution with optional `name` parameters
 */

inline fun <reified T : Any> DISdkComponent.inject(): Lazy<T> {
    return lazy(LazyThreadSafetyMode.SYNCHRONIZED) { getSdkContainer().resolve() }
}

inline fun <reified T : Any> DISdkComponent.resolve(): T {
    return getSdkContainer().resolve()
}

inline fun <reified T : Any> DISdkComponent.resolve(name: String): T {
    return getSdkContainer().resolve(name)
}

inline fun <reified T : DependencyContainer> DISdkComponent.registerContainer(containerProvider: (SdkContainer) -> T) {
    getSdkContainer().run {
        registerContainer(containerProvider(this))
    }
}

inline fun <reified T : DependencyContainer> DISdkComponent.unregisterContainer() {
    getSdkContainer().unregisterContainer<T>()
}

context(ViewModelStoreOwner)
inline fun <
    reified T : ViewModel,
    reified R : ViewModelProvider.Factory,
    > DISdkComponent.viewModel(): Lazy<T> {
    return lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        ViewModelProvider(
            this@ViewModelStoreOwner,
            getSdkContainer().resolve<R>(),
        )[T::class.java]
    }
}
