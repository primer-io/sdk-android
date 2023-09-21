package io.primer.android.components.manager.nolPay.unlinkCard.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.components.manager.nolPay.unlinkCard.component.NolPayUnlinkCardComponent
import io.primer.android.di.DIAppComponent
import io.primer.android.di.NOL_PAY_ERROR_RESOLVER_NAME
import org.koin.core.component.get
import org.koin.core.qualifier.named

internal class NolPayUnlinkCardComponentProvider : DIAppComponent {

    fun provideInstance(owner: ViewModelStoreOwner): NolPayUnlinkCardComponent {
        return ViewModelProvider(
            owner,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    return NolPayUnlinkCardComponent(
                        get(),
                        get(),
                        get(named(NOL_PAY_ERROR_RESOLVER_NAME)),
                        extras.createSavedStateHandle()
                    ) as T
                }
            }
        )[NolPayUnlinkCardComponent::class.java]
    }
}
