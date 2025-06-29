package io.primer.android.components.ui.views

import android.content.Context
import android.view.View
import android.view.ViewGroup

internal fun interface PaymentMethodViewCreator {
    fun create(
        context: Context,
        container: ViewGroup?,
    ): View
}
