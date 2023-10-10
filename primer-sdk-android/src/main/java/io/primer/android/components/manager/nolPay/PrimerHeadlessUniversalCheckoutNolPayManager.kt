package io.primer.android.components.manager.nolPay

import androidx.lifecycle.ViewModelStoreOwner
import io.primer.android.components.manager.nolPay.linkCard.component.NolPayLinkCardComponent
import io.primer.android.components.manager.nolPay.listCards.component.NolPayLinkedCardsComponent
import io.primer.android.components.manager.nolPay.nfc.component.NolPayNfcComponent
import io.primer.android.components.manager.nolPay.payment.component.NolPayPaymentComponent
import io.primer.android.components.manager.nolPay.unlinkCard.component.NolPayUnlinkCardComponent

/**
 * The [PrimerHeadlessUniversalCheckoutNolPayManager] class provides methods to obtain various components
 * related to NolPay integration in the Primer SDK. These components can be used to handle NolPay
 * functionalities within your application.
 */
class PrimerHeadlessUniversalCheckoutNolPayManager {

    /**
     * Provides an instance of the [NolPayLinkCardComponent] to manage linking a card for Nol Pay.
     *
     * @param viewModelStoreOwner The [ViewModelStoreOwner] to associate with the component.
     * @return An instance of [NolPayLinkCardComponent].
     */
    fun provideNolPayLinkCardComponent(viewModelStoreOwner: ViewModelStoreOwner):
        NolPayLinkCardComponent = NolPayLinkCardComponent.provideInstance(viewModelStoreOwner)

    /**
     * Provides an instance of the [NolPayUnlinkCardComponent] to manage unlinking a card from Nol Pay.
     *
     * @param viewModelStoreOwner The [ViewModelStoreOwner] to associate with the component.
     * @return An instance of [NolPayUnlinkCardComponent].
     */
    fun provideNolPayUnlinkCardComponent(viewModelStoreOwner: ViewModelStoreOwner):
        NolPayUnlinkCardComponent = NolPayUnlinkCardComponent.provideInstance(viewModelStoreOwner)

    /**
     * Provides an instance of the [NolPayPaymentComponent] to handle Nol Pay payments.
     *
     * @param viewModelStoreOwner The [ViewModelStoreOwner] to associate with the component.
     * @return An instance of [NolPayPaymentComponent].
     */
    fun provideNolPayPaymentComponent(viewModelStoreOwner: ViewModelStoreOwner):
        NolPayPaymentComponent = NolPayPaymentComponent.getInstance(viewModelStoreOwner)

    /**
     * Provides an instance of the [NolPayLinkedCardsComponent] to manage linked cards for Nol Pay.
     *
     * @return An instance of [NolPayLinkedCardsComponent].
     */
    fun provideNolPayLinkedCardsComponent() = NolPayLinkedCardsComponent.getInstance()

    /**
     * Provides an instance of the [NolPayNfcComponent] to handle Nol Pay NFC functionality.
     *
     * @return An instance of [NolPayNfcComponent].
     */
    fun provideNolPayNfcComponent() = NolPayNfcComponent.provideInstance()
}
