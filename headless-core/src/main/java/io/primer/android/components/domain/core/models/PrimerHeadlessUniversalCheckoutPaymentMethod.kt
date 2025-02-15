package io.primer.android.components.domain.core.models

import io.primer.android.PrimerSessionIntent
import io.primer.android.paymentmethods.PrimerRawData
import kotlin.reflect.KClass

/**
 * A data class representing a payment method in the Primer checkout flow.
 *
 * @property paymentMethodType The type of the payment method (e.g., "ADYEN_IDEAL", "GOOGLE_PAY").
 * @property paymentMethodName The name of the payment method (e.g., "Adyen iDeal", "Google Pay"), or null if
 * unavailable.
 * @property supportedPrimerSessionIntents A list of [PrimerSessionIntent] that this payment method supports.
 * @property paymentMethodManagerCategories A list of [PrimerPaymentMethodManagerCategory] that categorize this payment
 * method.
 * @property requiredInputDataClass An optional [KClass] representing the input data class required for this payment
 * method, or null if no such input data is required.
 */
data class PrimerHeadlessUniversalCheckoutPaymentMethod(
    val paymentMethodType: String,
    val paymentMethodName: String?,
    val supportedPrimerSessionIntents: List<PrimerSessionIntent>,
    val paymentMethodManagerCategories: List<PrimerPaymentMethodManagerCategory>,
    val requiredInputDataClass: KClass<out PrimerRawData>? = null,
)
