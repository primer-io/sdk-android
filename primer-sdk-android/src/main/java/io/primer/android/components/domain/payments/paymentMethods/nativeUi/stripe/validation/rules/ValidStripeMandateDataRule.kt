package io.primer.android.components.domain.payments.paymentMethods.nativeUi.stripe.validation.rules

import io.primer.android.analytics.data.models.SdkIntegrationType
import io.primer.android.components.domain.core.validation.ValidationResult
import io.primer.android.components.domain.core.validation.ValidationRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.stripe.exception.StripeIllegalValueKey
import io.primer.android.data.base.exceptions.IllegalValueException
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.settings.PrimerStripeOptions

internal class ValidStripeMandateDataRule(
    private val primerSettings: PrimerSettings
) : ValidationRule<PrimerStripeOptions> {
    override fun validate(t: PrimerStripeOptions): ValidationResult =
        when (primerSettings.sdkIntegrationType) {
            SdkIntegrationType.DROP_IN -> {
                when (t.mandateData) {
                    null -> ValidationResult.Failure(
                        IllegalValueException(
                            key = StripeIllegalValueKey.STRIPE_MANDATE_DATA,
                            message = "Required value for " +
                                "${StripeIllegalValueKey.STRIPE_MANDATE_DATA.key} was null."
                        )
                    )

                    else -> ValidationResult.Success
                }
            }
            SdkIntegrationType.HEADLESS -> ValidationResult.Success
        }
}
