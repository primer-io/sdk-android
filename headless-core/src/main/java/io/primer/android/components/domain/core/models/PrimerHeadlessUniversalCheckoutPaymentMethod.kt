package io.primer.android.components.domain.core.models

import io.primer.android.PrimerSessionIntent
import io.primer.android.paymentmethods.PrimerRawData
import kotlin.reflect.KClass

data class PrimerHeadlessUniversalCheckoutPaymentMethod(
    val paymentMethodType: String,
    val paymentMethodName: String?,
    val supportedPrimerSessionIntents: List<PrimerSessionIntent>,
    val paymentMethodManagerCategories: List<PrimerPaymentMethodManagerCategory>,
    val requiredInputDataClass: KClass<out PrimerRawData>? = null
)
