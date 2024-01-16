package io.primer.android.components.manager.banks.composable

import io.primer.android.components.manager.core.composable.PrimerCollectableData

/**
 * A sealed interface representing collectable data needed for bank related payments.
 */
sealed interface BanksCollectableData : PrimerCollectableData {
    /**
     * Data class representing the bank list filter.
     *
     * @property text The text to filter the bank list by.
     */
    data class Filter(val text: String) : BanksCollectableData

    /**
     * Data class representing the id of the selected bank.
     *
     * @property id The id of the selected bank.
     */
    data class BankId(val id: String) : BanksCollectableData
}
