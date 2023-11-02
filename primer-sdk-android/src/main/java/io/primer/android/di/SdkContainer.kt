package io.primer.android.di

import androidx.annotation.VisibleForTesting

internal class SdkContainer {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal val containers = mutableMapOf<String, DependencyContainer>()

    inline fun <reified T : DependencyContainer> registerContainer(container: T) {
        containers[T::class.java.name] = container.apply { registerInitialDependencies() }
    }

    inline fun <reified T : DependencyContainer> unregisterContainer() {
        containers.remove(T::class.java.name)?.apply { unregisterAll() }
    }

    fun clear() {
        containers.forEach { entry -> entry.value.unregisterAll() }
        containers.clear()
    }

    inline fun <reified T : Any> resolve(): T {
        return resolve(T::class.java.name)
    }

    inline fun <reified T : Any> resolve(dependencyName: String): T {
        val dependencyErrorChain = linkedSetOf<String?>()
        return containers.values.firstNotNullOfOrNull { container ->
            try {
                container.resolve(dependencyName)
            } catch (expected: Exception) {
                dependencyErrorChain.add(expected.message)
                null
            }
        } ?: error(
            "Unable to resolve type ${T::class.java.name} with dependency chain:" +
                " ${dependencyErrorChain.filterNotNull().joinToString(" -> ")}"
        )
    }
}
