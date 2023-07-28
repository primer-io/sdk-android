package io.primer.android.components.manager.raw

import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.data.payments.configure.PrimerInitializationData
import io.primer.android.domain.error.models.PrimerError

@JvmDefaultWithCompatibility
interface PrimerHeadlessUniversalCheckoutRawDataManagerInterface {

    /**
     * This method should be called when payment method require to preload
     * additional data, like retail outlets or banks list.
     */
    fun configure(completion: (PrimerInitializationData?, PrimerError?) -> Unit)

    /**
     * Configures the PrimerHeadlessUniversalCheckoutRawDataManager
     * with [PrimerHeadlessUniversalCheckoutRawDataManagerListener].
     */
    fun setListener(listener: PrimerHeadlessUniversalCheckoutRawDataManagerListener)

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
     * it is in the same state as newly created [PrimerHeadlessUniversalCheckoutRawDataManager]
     * and can be used once again, but should go through [setListener] once again.
     */
    fun cleanup()
}
