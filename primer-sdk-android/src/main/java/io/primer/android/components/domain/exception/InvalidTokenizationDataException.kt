package io.primer.android.components.domain.exception

import io.primer.android.components.domain.core.models.PrimerRawData
import kotlin.reflect.KClass

internal class InvalidTokenizationDataException(
    val paymentMethodType: String,
    val inputData: KClass<out PrimerRawData>,
    val requiredInputData: KClass<out PrimerRawData>?
) : IllegalStateException()
