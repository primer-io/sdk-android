package io.primer.android.payment.async.promptpay

import io.primer.android.R
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.payments.additionalInfo.OmiseCheckoutAdditionalInfoResolver
import io.primer.android.domain.payments.additionalInfo.PrimerCheckoutAdditionalInfoResolver
import io.primer.android.payment.HeadlessDefinition
import io.primer.android.payment.async.AsyncPaymentMethod
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.ui.payment.LoadingState

internal class OmisePromptPayPaymentMethodDescriptor(
    override val options: AsyncPaymentMethod,
    localConfig: PrimerConfig,
    config: PaymentMethodConfigDataResponse
) : AsyncPaymentMethodDescriptor(options, localConfig, config) {

    override fun getLoadingState() = LoadingState(
        if (localConfig.settings.uiOptions.theme.isDarkMode == true) {
            R.drawable.ic_logo_promptpay_dark
        } else { R.drawable.ic_logo_promptpay_light }
    )

    override val additionalInfoResolver: PrimerCheckoutAdditionalInfoResolver
        get() = OmiseCheckoutAdditionalInfoResolver()

    override val headlessDefinition: HeadlessDefinition
        get() = HeadlessDefinition(listOf(PrimerPaymentMethodManagerCategory.NATIVE_UI))
}
