package io.primer.ui_components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.PrimerSessionIntent
import io.primer.android.card.implementation.composer.presentation.provider.CardComposerProviderFactory
import io.primer.android.core.di.extensions.resolve
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.klarna.PrimerHeadlessUniversalCheckoutKlarnaManager
import io.primer.android.klarna.api.component.KlarnaComponent
import io.primer.android.nolpay.api.manager.unlinkCard.component.NolPayUnlinkCardComponent

class PrimerPaymentMethodComponent internal constructor() {


    companion object {

        fun provideInstance(
            owner: ViewModelStoreOwner,
            paymentMethodType: String,
            primerSessionIntent: PrimerSessionIntent
        ): KlarnaComponent {
            return ViewModelProvider(
                owner,
                object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(
                        modelClass: Class<T>,
                        extras: CreationExtras
                    ): T {
                        return PrimerHeadlessUniversalCheckoutKlarnaManager(viewModelStoreOwner = owner).provideKlarnaComponent(
                            primerSessionIntent
                        ) as T
                    }
                }
            ).get(
                key = KlarnaComponent::class.java.canonicalName.orEmpty(),
                modelClass = KlarnaComponent::class.java
            )
        }
    }
}