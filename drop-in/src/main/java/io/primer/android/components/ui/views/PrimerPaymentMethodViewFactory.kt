package io.primer.android.components.ui.views

import android.content.Context
import android.view.View
import android.view.ViewGroup
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.payment.config.BaseDisplayMetadata
import io.primer.android.payment.config.ImageDisplayMetadata
import io.primer.android.payment.config.TextDisplayMetadata
import io.primer.android.paymentMethods.core.ui.assets.AssetsManager
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType

internal class PrimerPaymentMethodViewFactory(
    val context: Context,
    val config: PrimerConfig,
    val assetsManager: AssetsManager
) {

    fun getViewForPaymentMethod(
        displayMetadata: BaseDisplayMetadata,
        container: ViewGroup?
    ): View {
        return when (PaymentMethodType.safeValueOf(displayMetadata.paymentMethodType)) {
            PaymentMethodType.PAYMENT_CARD,
            PaymentMethodType.IPAY88_CARD -> CreditCardViewCreator(config).create(
                context,
                container
            )

            else -> when (displayMetadata.type) {
                BaseDisplayMetadata.DisplayMetadataType.TEXT ->
                    DynamicPaymentMethodTextViewCreator(
                        theme = config.settings.uiOptions.theme,
                        displayMetadata = displayMetadata as TextDisplayMetadata,
                        assetsManager = assetsManager
                    ).create(context, container)

                BaseDisplayMetadata.DisplayMetadataType.IMAGE ->
                    DynamicPaymentMethodImageViewCreator(
                        theme = config.settings.uiOptions.theme,
                        displayMetadata = displayMetadata as ImageDisplayMetadata,
                        assetsManager = assetsManager
                    ).create(context, container)
            }
        }
    }
}
