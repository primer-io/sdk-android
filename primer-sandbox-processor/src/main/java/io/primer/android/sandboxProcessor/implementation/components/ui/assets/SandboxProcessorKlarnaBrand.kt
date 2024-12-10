package io.primer.android.sandboxProcessor.implementation.components.ui.assets

import io.primer.android.assets.ui.model.Brand
import io.primer.android.sandboxProcessor.R

internal class SandboxProcessorKlarnaBrand : Brand {
    override val iconResId: Int
        get() = R.drawable.ic_logo_klarna_dark

    override val logoResId: Int
        get() = R.drawable.ic_logo_klarna_square

    override val iconLightResId: Int
        get() = R.drawable.ic_logo_klarna
}
