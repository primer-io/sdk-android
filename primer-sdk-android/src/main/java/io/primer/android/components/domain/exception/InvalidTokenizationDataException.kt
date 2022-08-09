package io.primer.android.components.domain.exception

import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutInputData
import kotlin.reflect.KClass

internal class InvalidTokenizationDataException(
    val paymentMethodType: String,
    val inputData: KClass<out PrimerHeadlessUniversalCheckoutInputData>
) : IllegalStateException()
