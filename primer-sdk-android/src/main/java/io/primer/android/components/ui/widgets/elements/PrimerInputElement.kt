package io.primer.android.components.ui.widgets.elements

interface PrimerInputElement {
    fun setPrimerInputElementListener(listener: PrimerInputElementListener)
    fun getType(): PrimerInputElementType
    fun isValid(): Boolean
}
