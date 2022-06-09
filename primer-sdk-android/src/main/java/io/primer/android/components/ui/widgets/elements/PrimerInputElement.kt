package io.primer.android.components.ui.widgets.elements

import io.primer.android.ExperimentalPrimerApi

@ExperimentalPrimerApi
interface PrimerInputElement {
    fun setPrimerInputElementListener(listener: PrimerInputElementListener)
    fun getType(): PrimerInputElementType
    fun isValid(): Boolean
}
