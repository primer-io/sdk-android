package io.primer.android.components.domain.exception

import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutInputData
import io.primer.android.model.dto.PaymentMethodType
import kotlin.reflect.KClass

internal class InvalidTokenizationDataException(
    val paymentMethodType: PaymentMethodType,
    val inputData: KClass<out PrimerHeadlessUniversalCheckoutInputData>
) : IllegalStateException()
