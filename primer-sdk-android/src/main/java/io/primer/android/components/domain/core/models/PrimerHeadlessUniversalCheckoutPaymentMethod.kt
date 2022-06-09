package io.primer.android.components.domain.core.models

import io.primer.android.data.configuration.models.PrimerPaymentMethodType
import kotlin.reflect.KClass

data class PrimerHeadlessUniversalCheckoutPaymentMethod(
    val paymentMethodType: PrimerPaymentMethodType,
    val requiredInputDataClass: KClass<out PrimerHeadlessUniversalCheckoutInputData>? = null
)
