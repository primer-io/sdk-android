package io.primer.android.components.ui.widgets.elements

import io.primer.android.ui.CardNetwork

@JvmDefaultWithCompatibility
@Deprecated(
    "Card components will no longer receive ongoing maintenance and will be removed in future."
)
interface PrimerInputElementListener {
    fun inputElementValueChanged(inputElement: PrimerInputElement) = Unit
    fun inputElementValueIsValid(inputElement: PrimerInputElement, isValid: Boolean) = Unit
    fun inputElementDidDetectCardType(network: CardNetwork.Type) = Unit
}
