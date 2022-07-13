package io.primer.android.components.ui.widgets.elements

import io.primer.android.ExperimentalPrimerApi
import io.primer.android.components.domain.inputs.models.PrimerInputElementType

@ExperimentalPrimerApi
interface PrimerInputElement {
    fun setPrimerInputElementListener(listener: PrimerInputElementListener)
    fun getType(): PrimerInputElementType
    fun isValid(): Boolean
}
