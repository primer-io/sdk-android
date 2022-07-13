package io.primer.android.components.ui.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import io.primer.android.ExperimentalPrimerApi
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.ui.widgets.elements.PrimerInputElement
import io.primer.android.components.ui.widgets.elements.PrimerInputElementListener

@ExperimentalPrimerApi
internal class PrimerCountryCodeTextView(context: Context, attrs: AttributeSet? = null) :
    AppCompatTextView(context, attrs), PrimerInputElement {

    override fun setPrimerInputElementListener(listener: PrimerInputElementListener) = Unit

    override fun getType() = PrimerInputElementType.COUNTRY_CODE

    override fun isValid(): Boolean = super.getText()?.trim().isNullOrBlank().not()
}
