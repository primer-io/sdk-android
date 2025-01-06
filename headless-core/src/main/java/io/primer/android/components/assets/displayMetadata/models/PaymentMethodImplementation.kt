package io.primer.android.components.assets.displayMetadata.models

import io.primer.android.configuration.data.model.IconPosition
import io.primer.android.displayMetadata.domain.model.IconDisplayMetadata

data class PaymentMethodImplementation(
    val paymentMethodType: String,
    val name: String?,
    val buttonMetadata: ButtonMetadata?,
) {
    data class ButtonMetadata(
        val iconDisplayMetadata: List<IconDisplayMetadata>,
        val backgroundColor: ColorMetadata?,
        val borderColor: ColorMetadata?,
        val borderWidth: BorderWidthMetadata?,
        val cornerRadius: Float?,
        val text: String?,
        val textColor: ColorMetadata?,
        val iconPosition: IconPosition?,
    ) {
        data class ColorMetadata(
            val colored: String?,
            val light: String?,
            val dark: String?,
        )

        data class BorderWidthMetadata(
            val colored: Float?,
            val light: Float?,
            val dark: Float?,
        )
    }
}
