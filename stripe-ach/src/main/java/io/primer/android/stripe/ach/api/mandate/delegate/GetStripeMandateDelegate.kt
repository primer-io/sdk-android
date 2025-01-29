package io.primer.android.stripe.ach.api.mandate.delegate

import android.content.res.Resources
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.settings.PrimerStripeOptions
import io.primer.android.errors.utils.requireNotNullCheck
import io.primer.android.stripe.ach.implementation.session.data.exception.StripeIllegalValueKey
import io.primer.android.stripeach.R

class GetStripeMandateDelegate(
    private val resources: Resources,
    private val primerSettings: PrimerSettings,
) {
    operator fun invoke(): Result<String> =
        runCatching {
            val mandate =
                requireNotNullCheck(
                    primerSettings.paymentMethodOptions.stripeOptions.mandateData,
                    StripeIllegalValueKey.MISSING_MANDATE_DATA,
                )

            when (mandate) {
                is PrimerStripeOptions.MandateData.TemplateMandateData ->
                    resources.getString(R.string.stripe_ach_mandate_template_android, mandate.merchantName)

                is PrimerStripeOptions.MandateData.FullMandateStringData -> mandate.value
                is PrimerStripeOptions.MandateData.FullMandateData -> resources.getString(mandate.value)
            }
        }
}
