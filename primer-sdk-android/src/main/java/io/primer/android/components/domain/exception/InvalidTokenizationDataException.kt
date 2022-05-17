package io.primer.android.components.domain.exception

import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutInputData
import io.primer.android.data.configuration.models.PrimerPaymentMethodType
import kotlin.reflect.KClass

internal class InvalidTokenizationDataException(
    val paymentMethodType: PrimerPaymentMethodType,
    val inputData: KClass<out PrimerHeadlessUniversalCheckoutInputData>
) : IllegalStateException()
