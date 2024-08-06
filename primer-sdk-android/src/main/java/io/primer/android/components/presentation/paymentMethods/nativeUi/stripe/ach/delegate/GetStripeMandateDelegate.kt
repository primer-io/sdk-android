package io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate

import android.content.res.Resources
import io.primer.android.R
import io.primer.android.components.data.payments.paymentMethods.nativeUi.stripe.ach.exception.StripeIllegalValueKey
import io.primer.android.data.base.util.requireNotNullCheck
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.settings.PrimerStripeOptions

internal class GetStripeMandateDelegate(
    private val resources: Resources,
    private val primerSettings: PrimerSettings
) {
    operator fun invoke(): Result<String> = runCatching {
        val mandate = requireNotNullCheck(
            primerSettings.paymentMethodOptions.stripeOptions.mandateData,
            StripeIllegalValueKey.MISSING_MANDATE_DATA
        )

        when (mandate) {
            is PrimerStripeOptions.MandateData.TemplateMandateData ->
                resources.getString(R.string.stripe_ach_mandate_template_android, mandate.merchantName)

            is PrimerStripeOptions.MandateData.FullMandateData -> resources.getString(mandate.value)
        }
    }
}
