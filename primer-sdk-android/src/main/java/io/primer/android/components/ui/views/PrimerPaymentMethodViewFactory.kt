package io.primer.android.components.ui.views

import android.content.Context
import android.view.View
import android.view.ViewGroup
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.payment.config.BaseDisplayMetadata
import io.primer.android.payment.config.ImageDisplayMetadata
import io.primer.android.payment.config.TextDisplayMetadata

internal class PrimerPaymentMethodViewFactory(
    val context: Context,
    val config: PrimerConfig
) {

    fun getViewForPaymentMethod(
        displayMetadata: BaseDisplayMetadata,
        container: ViewGroup?
    ): View {
        return when (PaymentMethodType.safeValueOf(displayMetadata.paymentMethodType)) {
            PaymentMethodType.PAYMENT_CARD -> CreditCardViewCreator(config).create(
                context,
                container
            )
            PaymentMethodType.APAYA -> ApayaViewCreator(config.settings.uiOptions.theme).create(
                context,
                container
            )
            PaymentMethodType.GOCARDLESS -> GoCardlessViewCreator(
                config.settings.uiOptions.theme
            ).create(context, container)
            else -> when (displayMetadata.type) {
                BaseDisplayMetadata.DisplayMetadataType.TEXT ->
                    DynamicPaymentMethodTextViewCreator(
                        config.settings.uiOptions.theme,
                        displayMetadata as TextDisplayMetadata

                    ).create(context, container)
                BaseDisplayMetadata.DisplayMetadataType.IMAGE ->
                    DynamicPaymentMethodImageViewCreator(
                        config.settings.uiOptions.theme,
                        displayMetadata as ImageDisplayMetadata
                    ).create(context, container)
            }
        }
    }
}
