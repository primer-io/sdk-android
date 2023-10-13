package io.primer.android.di.extension

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import io.primer.android.di.DISdkComponent

internal inline fun <reified T : Any> DISdkComponent.inject(): Lazy<T> {
    return lazy(LazyThreadSafetyMode.SYNCHRONIZED) { getSdkContainer().resolve() }
}

internal inline fun <reified T : Any> DISdkComponent.resolve(): T {
    return getSdkContainer().resolve()
}

context(ViewModelStoreOwner)
internal inline fun <reified T : ViewModel,
    reified R : ViewModelProvider.Factory> DISdkComponent.viewModel(): Lazy<T> {
    return lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        ViewModelProvider(
            this@ViewModelStoreOwner,
            getSdkContainer().resolve<R>()
        )[T::class.java]
    }
}

context(Fragment)
internal inline fun <reified T : ViewModel,
    reified R : ViewModelProvider.Factory> DISdkComponent.activityViewModel(): Lazy<T> {
    return lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        ViewModelProvider(
            this@Fragment.requireActivity(),
            getSdkContainer().resolve<R>()
        )[T::class.java]
    }
}
