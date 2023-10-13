package io.primer.android.components.manager.nolPay.payment.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.components.manager.nolPay.payment.component.NolPayPaymentComponent
import io.primer.android.di.DIAppComponent
import io.primer.android.di.NOL_PAY_ERROR_RESOLVER_NAME
import org.koin.core.component.get
import org.koin.core.qualifier.named

internal class NolPayStartPaymentComponentProvider : DIAppComponent {

    fun provideInstance(owner: ViewModelStoreOwner): NolPayPaymentComponent {
        return ViewModelProvider(
            owner,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    return NolPayPaymentComponent(
                        get(),
                        get(),
                        get(),
                        get(named(NOL_PAY_ERROR_RESOLVER_NAME)),
                    ) as T
                }
            }
        )[NolPayPaymentComponent::class.java]
    }
}
