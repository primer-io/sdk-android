package io.primer.android.core.di.extensions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer

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
inline fun <reified T : ViewModel,
    reified R : ViewModelProvider.Factory> DISdkComponent.viewModel(): Lazy<T> {
    return lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        ViewModelProvider(
            this@ViewModelStoreOwner,
            getSdkContainer().resolve<R>()
        )[T::class.java]
    }
}
