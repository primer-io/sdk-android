package io.primer.android.components.manager.banks.composable

import io.primer.android.components.manager.core.composable.PrimerHeadlessStep
import io.primer.android.domain.rpc.banks.models.IssuingBank

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
