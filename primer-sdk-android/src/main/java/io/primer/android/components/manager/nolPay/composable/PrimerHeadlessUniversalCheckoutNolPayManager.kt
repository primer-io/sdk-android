package io.primer.android.components.manager.nolPay.composable

import androidx.lifecycle.ViewModelStoreOwner
import io.primer.android.components.manager.nolPay.NolPayLinkCardComponent
import io.primer.android.components.manager.nolPay.NolPayLinkedCardsComponent
import io.primer.android.components.manager.nolPay.NolPayStartPaymentComponent
import io.primer.android.components.manager.nolPay.NolPayUnlinkCardComponent

class PrimerHeadlessUniversalCheckoutNolPayManager {

    fun provideNolPayLinkCardComponent(owner: ViewModelStoreOwner): NolPayLinkCardComponent =
        NolPayLinkCardComponent.getInstance(owner)

    fun provideNolPayUnlinkCardComponent(owner: ViewModelStoreOwner): NolPayUnlinkCardComponent =
        NolPayUnlinkCardComponent.getInstance(owner)

    fun provideNolPayStartPaymentComponent(owner: ViewModelStoreOwner): NolPayStartPaymentComponent =
        NolPayStartPaymentComponent.getInstance(owner)

    fun provideNolPayLinkedCardsComponent() = NolPayLinkedCardsComponent()
}
