package io.primer.android.sandboxProcessor.implementation.components.ui.assets

import io.primer.android.assets.ui.model.Brand
import io.primer.android.sandboxProcessor.R

internal class SandboxProcessorPayPalBrand : Brand {
    override val iconResId: Int
        get() = R.drawable.ic_logo_paypal

    override val logoResId: Int
        get() = R.drawable.ic_logo_paypal_square
}
