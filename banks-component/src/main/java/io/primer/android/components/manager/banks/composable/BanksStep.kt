package io.primer.android.components.manager.banks.composable

import io.primer.android.banks.implementation.rpc.domain.models.IssuingBank
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessStep

/**
 * A sealed interface representing the steps involved in a bank related payment process.
 */
sealed interface BanksStep : PrimerHeadlessStep {
    /**
     * Object representing the loading of the bank list.
     */
    object Loading : BanksStep

    /**
     * A data class representing the list of retrieved banks.
     *
     * @property banks The list of retrieved banks.
     */
    data class BanksRetrieved(val banks: List<IssuingBank>) : BanksStep
}
