package io.primer.android.components.domain.core.models

import kotlin.reflect.KClass

data class PrimerHeadlessUniversalCheckoutPaymentMethod(
    val paymentMethodType: String,
    val requiredInputDataClass: KClass<out PrimerRawData>? = null
)
