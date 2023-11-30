package io.primer.android.components.ui.widgets.elements

import io.primer.android.components.domain.inputs.models.PrimerInputElementType

@Deprecated(
    "Card components will no longer receive ongoing maintenance and will be removed in future."
)
interface PrimerInputElement {
    fun setPrimerInputElementListener(listener: PrimerInputElementListener)
    fun getType(): PrimerInputElementType
    fun isValid(): Boolean
}
