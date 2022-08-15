package io.primer.android.components.manager.raw

import io.primer.android.ExperimentalPrimerApi
import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.inputs.models.PrimerInputElementType

@JvmDefaultWithCompatibility
@ExperimentalPrimerApi
interface PrimerHeadlessUniversalCheckoutRawDataManagerInterface {

    /**
     * Configures the PrimerHeadlessUniversalCheckoutRawDataManager with [PrimerHeadlessUniversalCheckoutRawDataManagerListener].
     */
    fun setManagerListener(listener: PrimerHeadlessUniversalCheckoutRawDataManagerListener)

    /**
     * Sets the current [PrimerRawData].
     * This method will invoke validation and metadata checks on each change.
     * These will invoke [PrimerHeadlessUniversalCheckoutRawDataManagerListener] callbacks.
     */
    fun setRawData(rawData: PrimerRawData)

    /**
     * Submits the [PrimerRawData] previously set by [setRawData].
     */
    fun submit()

    /**
     * Lists the [PrimerInputElementType] required by PrimerHeadlessUniversalCheckoutRawDataManager
     */
    fun getRequiredInputElementTypes(): List<PrimerInputElementType>

    /**
     * This method should be called when disposing the listener in order to free Primer SDK resource.
     * Once instance of [PrimerHeadlessUniversalCheckoutRawDataManager] has freed up the used resources,
     * it is in the same state as newly created [PrimerHeadlessUniversalCheckoutRawDataManager] and can be used once again,
     * but should go through [setManagerListener] once again.
     */
    fun cleanup()
}
