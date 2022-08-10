package io.primer.android.domain.payments.displayMetadata.models

import io.primer.android.data.configuration.models.IconPosition
import io.primer.android.data.payments.displayMetadata.model.IconDisplayMetadata

internal data class PaymentMethodImplementation(
    val paymentMethodType: String,
    val name: String?,
    val buttonMetadata: ButtonMetadata?
) {

    internal data class ButtonMetadata(
        val iconDisplayMetadata: List<IconDisplayMetadata>,
        val backgroundColor: ColorMetadata?,
        val borderColor: ColorMetadata?,
        val borderWidth: BorderWidthMetadata?,
        val cornerRadius: Float?,
        val text: String?,
        val textColor: ColorMetadata?,
        val iconPosition: IconPosition?
    ) {

        internal data class ColorMetadata(
            val colored: String?,
            val light: String?,
            val dark: String?
        )

        internal data class BorderWidthMetadata(
            val colored: Float?,
            val light: Float?,
            val dark: Float?
        )
    }
}
