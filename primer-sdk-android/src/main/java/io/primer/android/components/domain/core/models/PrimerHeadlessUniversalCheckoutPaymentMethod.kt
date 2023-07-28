package io.primer.android.components.domain.core.models

import io.primer.android.PrimerSessionIntent
import kotlin.reflect.KClass

data class PrimerHeadlessUniversalCheckoutPaymentMethod(
    val paymentMethodType: String,
    val supportedPrimerSessionIntents: List<PrimerSessionIntent>,
    val paymentMethodManagerCategories: List<PrimerPaymentMethodManagerCategory>,
    val requiredInputDataClass: KClass<out PrimerRawData>? = null
)

enum class PrimerPaymentMethodManagerCategory {
    NATIVE_UI,
    RAW_DATA,
    CARD_COMPONENTS
}
