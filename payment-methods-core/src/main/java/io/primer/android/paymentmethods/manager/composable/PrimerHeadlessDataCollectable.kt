package io.primer.android.paymentmethods.manager.composable

interface PrimerCollectableData

/**
 * A contract for classes that can collect and update data of type `T`.
 *
 * @param T The type of data to be collected and updated, which must implement the [PrimerCollectableData] interface.
 */
interface PrimerHeadlessDataCollectable<T : PrimerCollectableData?> {
    /**
     * Update the collected data with new information.
     *
     * @param collectedData The new data to update the collected data with.
     */
    fun updateCollectedData(collectedData: T)
}
