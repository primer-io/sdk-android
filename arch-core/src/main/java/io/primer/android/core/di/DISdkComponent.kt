package io.primer.android.core.di

/**
 * Core dependency injection component interface for the Primer Android SDK.
 *
 * This interface provides unified access to the SDK's dependency containers across different
 * SDK modes (Drop-in, Headless, and Components). It acts as the primary entry point for
 * dependency resolution throughout the SDK architecture.
 *
 * ## Architecture
 * `DISdkComponent` delegates container resolution to [DISdkContext], which manages different
 * container types based on the current SDK mode. The context automatically merges core
 * dependencies with mode-specific dependencies to provide a unified dependency graph.
 *
 * ## When to Use
 * Use `DISdkComponent` when you need to:
 * - Resolve dependencies in ViewModels, Use Cases, or Repositories
 * - Access SDK services in payment method implementations
 * - Create ViewModel factories that require dependency injection
 * - Implement component providers that need SDK dependencies
 *
 * ## Usage Pattern
 * Most implementations follow the anonymous object pattern:
 * ```kotlin
 * val component = object : DISdkComponent {}
 * val dependency = component.getSdkContainer().resolve<SomeDependency>()
 * ```
 *
 * For lazy resolution (recommended for better performance):
 * ```kotlin
 * class SomeClass(private val diSdkComponent: DISdkComponent) {
 *     private val repository by lazy {
 *         diSdkComponent.getSdkContainer().resolve<SomeRepository>()
 *     }
 * }
 * ```
 *
 * @see SdkComponent Base interface for SDK component contracts
 * @see DISdkContext Global context managing SDK container lifecycle
 * @see SdkContainer Container implementation for dependency management
 */
interface DISdkComponent : SdkComponent {
    override fun getSdkContainer(): SdkContainer {
        return DISdkContext.container()
    }
}
