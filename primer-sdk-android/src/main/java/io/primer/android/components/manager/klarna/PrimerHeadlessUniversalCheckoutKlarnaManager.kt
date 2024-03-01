package io.primer.android.components.manager.klarna

import androidx.lifecycle.ViewModelStoreOwner
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.composable.KlarnaComponent
import io.primer.android.di.DISdkComponent

/**
 * The [PrimerHeadlessUniversalCheckoutKlarnaManager] class provides a method to obtain
 * a component related to Klarna integration in the Primer SDK. This component can be used to handle
 * Klarna functionalities within your application.
 *
 * @param viewModelStoreOwner The [ViewModelStoreOwner] to associate with the component.
 */
class PrimerHeadlessUniversalCheckoutKlarnaManager(
    private val viewModelStoreOwner: ViewModelStoreOwner
) : DISdkComponent {
    /**
     * Provides an instance of the [KlarnaComponent] to handle Klarna session.
     *
     * @param primerSessionIntent The type of the [PrimerSessionIntent] to be used during session
     * creation and tokenization.
     * @return An instance of [KlarnaComponent].
     */
    fun provideKlarnaComponent(primerSessionIntent: PrimerSessionIntent): KlarnaComponent {
        return KlarnaComponent.provideInstance(viewModelStoreOwner, primerSessionIntent)
    }
}
