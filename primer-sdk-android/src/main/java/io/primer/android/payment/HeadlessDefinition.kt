package io.primer.android.payment

import io.primer.android.components.domain.core.mapper.PrimerPaymentMethodRawDataMapper
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.domain.core.models.PrimerRawData
import kotlin.reflect.KClass

internal class HeadlessDefinition(
    val paymentMethodManagerCategories: List<PrimerPaymentMethodManagerCategory>,
    val rawDataDefinition: RawDataDefinition? = null
) {
    class RawDataDefinition(
        val requiredInputDataClass: KClass<out PrimerRawData>,
        val rawDataMapper: PrimerPaymentMethodRawDataMapper<PrimerRawData>
    )
}
