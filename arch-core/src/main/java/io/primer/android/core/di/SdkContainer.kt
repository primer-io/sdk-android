package io.primer.android.core.di

import java.util.concurrent.ConcurrentHashMap

operator fun SdkContainer?.plus(other: SdkContainer?): SdkContainer? {
    if (this == null && other == null) return null

    val mergedSdkContainer = SdkContainer()

    this?.containers?.forEach { (name, container) ->
        mergedSdkContainer.containers[name] = container
    }

    other?.containers?.forEach { (name, container) ->
        mergedSdkContainer.containers[name] = container
    }
    return mergedSdkContainer
}

class SdkContainer {
    var containers = ConcurrentHashMap<String, DependencyContainer>()

    inline fun <reified T : DependencyContainer> registerContainer(container: T) {
        containers[T::class.java.name] = container.apply { registerInitialDependencies() }
    }

    inline fun <reified T : DependencyContainer> registerContainer(
        name: String,
        container: T,
    ) {
        containers[name] = container.apply { registerInitialDependencies() }
    }

    inline fun <reified T : DependencyContainer> unregisterContainer() {
        containers.remove(T::class.java.name)?.apply { unregisterAll() }
    }

    inline fun <reified T : DependencyContainer> unregisterContainer(name: String) {
        containers.remove(name)?.apply { unregisterAll() }
    }

    fun clear() {
        containers.forEach { entry -> entry.value.unregisterAll() }
        containers.clear()
    }

    /**
     * Unregister the given [type][T] from all containers.
     */
    inline fun <reified T : Any> unregisterType() {
        containers.forEach { it.value.unregister<T>() }
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
                " ${dependencyErrorChain.filterNotNull().joinToString(" -> ")}",
        )
    }
}
