package io.primer.android.components.ui.views

import android.content.Context
import android.view.View
import android.view.ViewGroup

internal interface PaymentMethodViewCreator {

    fun create(context: Context, container: ViewGroup?): View

    companion object {
        const val DEFAULT_EXPORTED_ICON_SCALE = 3.0f
    }
}
