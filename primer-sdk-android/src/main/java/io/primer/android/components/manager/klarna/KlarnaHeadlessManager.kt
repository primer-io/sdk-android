package io.primer.android.components.manager.klarna

import androidx.lifecycle.ViewModelStoreOwner
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.composable.KlarnaPaymentComponent

/**
 * The [KlarnaHeadlessManager] class provides methods to obtain various components
 * related to Klarna integration in the Primer SDK. These components can be used to handle Klarna
 * functionalities within your application.
 */
class KlarnaHeadlessManager {

    /**
     * Provides an instance of the [KlarnaPaymentComponent] to handle Klarna session.
     *
     * @param viewModelStoreOwner The [ViewModelStoreOwner] to associate with the component.
     * @return An instance of [KlarnaPaymentComponent].
     */
    fun provideKlarnaPaymentComponent(viewModelStoreOwner: ViewModelStoreOwner):
        KlarnaPaymentComponent = KlarnaPaymentComponent.provideInstance(viewModelStoreOwner)
}
