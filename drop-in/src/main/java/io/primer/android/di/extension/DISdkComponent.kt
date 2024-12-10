package io.primer.android.di.extension

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.primer.android.core.di.DISdkComponent

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
