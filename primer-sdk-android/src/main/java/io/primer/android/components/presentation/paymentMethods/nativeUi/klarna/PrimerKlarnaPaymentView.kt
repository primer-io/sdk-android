package io.primer.android.components.presentation.paymentMethods.nativeUi.klarna

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.AttrRes

class PrimerKlarnaPaymentView(
    context: Context,
    attributeSet: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    private val doAuthorize: () -> Unit
) : FrameLayout(context, attributeSet, defStyleAttr) {
    constructor(
        context: Context,
        attributeSet: AttributeSet? = null,
        @AttrRes defStyleAttr: Int = 0
    ) : this(
        context = context,
        attributeSet = attributeSet,
        defStyleAttr = defStyleAttr,
        doAuthorize = {}
    )

    fun authorize() = doAuthorize()
}
