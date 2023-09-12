package io.primer.sample.test

import io.primer.android.components.domain.error.PrimerValidationError
import kotlinx.coroutines.flow.Flow

interface PrimerHeadlessValidatable {

    val validationFlow: Flow<List<PrimerValidationError>>
}