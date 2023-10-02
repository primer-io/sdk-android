package io.primer.android.components.manager.nolPay

import androidx.lifecycle.ViewModelStoreOwner
import io.primer.android.components.manager.nolPay.linkCard.component.NolPayLinkCardComponent
import io.primer.android.components.manager.nolPay.listCards.component.NolPayLinkedCardsComponent
import io.primer.android.components.manager.nolPay.nfc.component.NolPayNfcComponent
import io.primer.android.components.manager.nolPay.startPayment.component.NolPayStartPaymentComponent
import io.primer.android.components.manager.nolPay.unlinkCard.component.NolPayUnlinkCardComponent

class PrimerHeadlessUniversalCheckoutNolPayManager {

    fun provideNolPayLinkCardComponent(owner: ViewModelStoreOwner): NolPayLinkCardComponent =
        NolPayLinkCardComponent.provideInstance(owner)

    fun provideNolPayUnlinkCardComponent(owner: ViewModelStoreOwner): NolPayUnlinkCardComponent =
        NolPayUnlinkCardComponent.provideInstance(owner)

    fun provideNolPayStartPaymentComponent(owner: ViewModelStoreOwner):
        NolPayStartPaymentComponent = NolPayStartPaymentComponent.getInstance(owner)

    fun provideNolPayLinkedCardsComponent() = NolPayLinkedCardsComponent.getInstance()

    fun provideNolPayNfcComponent() = NolPayNfcComponent.provideInstance()
}
