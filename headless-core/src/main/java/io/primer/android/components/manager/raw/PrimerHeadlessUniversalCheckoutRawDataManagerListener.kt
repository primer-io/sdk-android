package io.primer.android.components.manager.raw

import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadata
import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadataState
import io.primer.android.components.domain.error.PrimerInputValidationError

/**
 * Interface for listening to events related to raw data management in [PrimerHeadlessUniversalCheckoutRawDataManager].
 */
@JvmDefaultWithCompatibility
interface PrimerHeadlessUniversalCheckoutRawDataManagerListener {
    /**
     * Called when the validation state changes.
     *
     * @param isValid Indicates whether the current state of input is valid or not.
     * @param errors A list of validation errors if the state is not valid.
     */
    fun onValidationChanged(
        isValid: Boolean,
        errors: List<PrimerInputValidationError>,
    )

    /**
     * Called when the metadata associated with the payment method changes.
     *
     * @param metadata The updated [PrimerPaymentMethodMetadata].
     */
    fun onMetadataChanged(metadata: PrimerPaymentMethodMetadata) = Unit

    /**
     * Called when the state of metadata associated with the payment method changes.
     *
     * @param metadataState The updated [PrimerPaymentMethodMetadataState].
     */
    fun onMetadataStateChanged(metadataState: PrimerPaymentMethodMetadataState) = Unit
}
