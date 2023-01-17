package io.primer.android.components.manager.raw

import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadata
import io.primer.android.components.domain.error.PrimerInputValidationError

@JvmDefaultWithCompatibility
interface PrimerHeadlessUniversalCheckoutRawDataManagerListener {

    fun onValidationChanged(isValid: Boolean, errors: List<PrimerInputValidationError>)

    fun onMetadataChanged(metadata: PrimerPaymentMethodMetadata) = Unit
}
