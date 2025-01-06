package io.primer.android.paymentmethods

import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import kotlin.reflect.KClass

data class HeadlessDefinition(
    val paymentMethodManagerCategories: List<PrimerPaymentMethodManagerCategory>,
    val rawDataDefinition: RawDataDefinition? = null,
) {
    data class RawDataDefinition(
        val requiredInputDataClass: KClass<out PrimerRawData>,
    )
}
