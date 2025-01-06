package io.primer.android.core.di

import kotlin.reflect.KClass

abstract class DependencyContainer {
    data class Key(val name: String, val type: KClass<*>)

    abstract fun registerInitialDependencies()

    val dependencies = mutableMapOf<Key, Any>()

    inline fun <reified T : Any> registerSingleton(noinline factory: () -> T) {
        dependencies[Key(T::class.java.name, T::class)] = lazy(factory)
    }

    inline fun <reified T : Any> registerSingleton(
        name: String,
        noinline factory: () -> T,
    ) {
        dependencies[Key(name, T::class)] = lazy(factory)
    }

    inline fun <reified T : Any> registerFactory(noinline factory: () -> T) {
        dependencies[Key(T::class.java.name, T::class)] = factory
    }

    inline fun <reified T : Any> registerFactory(
        name: String,
        noinline factory: () -> T,
    ) {
        dependencies[Key(name, T::class)] = factory
    }

    inline fun <reified T : Any> unregister() {
        return unregister<T>(T::class.java.name)
    }

    inline fun <reified T : Any> unregister(name: String) {
        dependencies.remove(Key(name, T::class))
    }

    fun unregisterAll() {
        dependencies.clear()
    }

    inline fun <reified T : Any> resolve(): T {
        return resolve(T::class.java.name)
    }

    inline fun <reified T : Any> resolve(name: String): T {
        return when (val dependency = dependencies[Key(name, T::class)]) {
            is Function0<*> -> dependency() as T
            is Lazy<*> -> dependency.value as T
            else -> error("Unregistered type ${T::class} for a key $name")
        }
    }
}
