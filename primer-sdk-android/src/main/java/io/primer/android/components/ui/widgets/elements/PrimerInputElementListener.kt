package io.primer.android.components.ui.widgets.elements

import io.primer.android.ui.CardType

@JvmDefaultWithCompatibility
interface PrimerInputElementListener {
    fun inputElementValueChanged(inputElement: PrimerInputElement) = Unit
    fun inputElementValueIsValid(inputElement: PrimerInputElement, isValid: Boolean) = Unit
    fun inputElementDidDetectCardType(type: CardType.Type) = Unit
}
