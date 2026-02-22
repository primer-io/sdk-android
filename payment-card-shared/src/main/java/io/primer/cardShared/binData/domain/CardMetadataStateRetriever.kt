package io.primer.cardShared.binData.domain

import androidx.annotation.VisibleForTesting
import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.Severity
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.components.domain.core.models.card.PrimerBinData
import io.primer.android.components.domain.core.models.card.PrimerBinDataStatus
import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.core.models.card.PrimerCardMetadataState
import io.primer.android.components.domain.core.models.card.PrimerCardNetwork
import io.primer.android.components.domain.core.models.card.PrimerCardNetworksMetadata
import io.primer.android.components.domain.core.models.card.PrimerCardNumberEntryMetadata
import io.primer.android.components.domain.core.models.card.PrimerCardNumberEntryState
import io.primer.android.components.domain.core.models.card.ValidationSource
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.configuration.extension.sanitizedCardNumber
import io.primer.android.core.extensions.mapSuspendCatching
import io.primer.android.core.logging.internal.LogReporter
import io.primer.cardShared.PaymentRawDataMetadataStateRetriever
import io.primer.cardShared.networks.domain.repository.OrderedAllowedCardNetworksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withTimeout

class CardMetadataStateRetriever(
    private val binMetadataDataRepository: CardBinMetadataRepository,
    private val allowedCardNetworksRepository: OrderedAllowedCardNetworksRepository,
    private val cardMetadataCacheHelper: CardMetadataCacheHelper,
    private val analyticsRepository: AnalyticsRepository,
    private val logReporter: LogReporter,
) : PaymentRawDataMetadataStateRetriever<PrimerCardData, PrimerCardMetadataState> {
    @VisibleForTesting
    internal var lastInputData: PrimerCardData? = null

    private val _metadataState = MutableSharedFlow<PrimerCardMetadataState>()
    override val metadataState = _metadataState.distinctUntilChanged()

    private val _binData = MutableSharedFlow<PrimerBinData>(replay = 1)
    val binData: Flow<PrimerBinData> = _binData

    override suspend fun handleInputData(inputData: PrimerCardData) {
        val newCardNumber = inputData.cardNumber.sanitizedCardNumber()
        val lastCardNumber = lastInputData?.cardNumber?.sanitizedCardNumber()
        if (lastCardNumber?.take(MAX_BIN_LENGTH) != newCardNumber.take(MAX_BIN_LENGTH)) {
            updateLastInputData(null)
            when {
                newCardNumber.length >= MAX_BIN_LENGTH ->
                    getRemoteCardMetadata(newCardNumber)
                        .also { updateLastInputData(inputData) }

                else -> {
                    _binData.emit(
                        PrimerBinData(
                            preferred = null,
                            alternatives = emptyList(),
                            status = PrimerBinDataStatus.PARTIAL,
                            firstDigits = null,
                        ),
                    )
                    _metadataState.emit(
                        PrimerCardMetadataState.Fetched(
                            getLocalCardMetadata(newCardNumber, ValidationSource.LOCAL),
                            PrimerCardNumberEntryState(newCardNumber),
                        ),
                    ).also {
                        updateLastInputData(inputData)
                    }
                }
            }
        }
    }

    private suspend fun getRemoteCardMetadata(cardNumber: String) =
        withTimeout(
            BIN_CALL_TIMEOUT,
        ) {
            emitFetchingState(cardNumber)
            binMetadataDataRepository.getBinMetadata(
                cardNumber.take(MAX_BIN_LENGTH),
                ValidationSource.REMOTE,
            ).mapSuspendCatching { binMetadataResult ->
                val orderedAllowedCardNetworks =
                    allowedCardNetworksRepository.getOrderedAllowedCardNetworks()
                val sortedAllowedItems =
                    binMetadataResult.items.sortedByAllowedNetworks(orderedAllowedCardNetworks)
                val allowedNetworks = sortedAllowedItems.map { metadata ->
                    PrimerCardNetwork(
                        requireNotNull(metadata.network),
                        metadata.displayName,
                        true,
                    )
                }
                logCardNetworksFetchedEvent(binMetadataResult)
                val selectableNetworks = allowedNetworks.takeIf { primerCardNetworks ->
                    primerCardNetworks.size > MIN_SELECTABLE_NETWORKS_SIZE &&
                        allowsUserNetworkSelection(primerCardNetworks)
                }
                val sortedBinData = sortedAllowedItems.map { it.toPrimerCardBinData() }
                _binData.emit(
                    PrimerBinData(
                        preferred = sortedBinData.firstOrNull(),
                        alternatives = sortedBinData.drop(1),
                        status = if (sortedBinData.isNotEmpty()) {
                            PrimerBinDataStatus.COMPLETE
                        } else {
                            PrimerBinDataStatus.PARTIAL
                        },
                        firstDigits = binMetadataResult.firstDigits,
                    ),
                )
                PrimerCardNumberEntryMetadata(
                    selectableNetworks?.toCardNetworksMetadata(),
                    allowedNetworks.toCardNetworksMetadata(),
                    ValidationSource.REMOTE,
                )
            }.recoverCatching { throwable ->
                logReporter.warn("Remote card validation failed: ${throwable.message}")
                logCardNetworksFetchingErrorEvent(throwable)
                _binData.emit(
                    PrimerBinData(
                        preferred = null,
                        alternatives = emptyList(),
                        status = PrimerBinDataStatus.PARTIAL,
                        firstDigits = null,
                    ),
                )
                getLocalCardMetadata(cardNumber.take(MAX_BIN_LENGTH), ValidationSource.LOCAL_FALLBACK)
            }.onSuccess { cardNumberEntryMetadata ->
                saveCardNetworksMetadata(
                    cardNumber.take(MAX_BIN_LENGTH),
                    cardNumberEntryMetadata,
                )
                _metadataState.emit(
                    PrimerCardMetadataState.Fetched(
                        cardNumberEntryMetadata,
                        PrimerCardNumberEntryState(
                            cardNumber,
                        ),
                    ),
                )
            }
        }

    private suspend fun getLocalCardMetadata(
        bin: String,
        source: ValidationSource,
    ) = when (bin.isBlank()) {
        true ->
            PrimerCardNumberEntryMetadata(
                null,
                emptyList<PrimerCardNetwork>().toCardNetworksMetadata(),
                source,
            )

        false ->
            binMetadataDataRepository.getBinMetadata(bin, source).getOrThrow()
                .items.toSortedPrimerCardNetworks(
                    allowedCardNetworksRepository.getOrderedAllowedCardNetworks(),
                )
                .filter { it.allowed }
                .let { primerCardNetworks ->
                    PrimerCardNumberEntryMetadata(
                        null,
                        primerCardNetworks.toCardNetworksMetadata(),
                        source,
                    )
                }.also {
                    when (source) {
                        ValidationSource.REMOTE -> Unit
                        ValidationSource.LOCAL_FALLBACK -> {
                            logReporter.warn(REMOTE_VALIDATION_FAILED_MESSAGE)
                            logCardNetworksLocalFallbackEvent()
                        }

                        ValidationSource.LOCAL -> Unit
                    }
                }
    }.also { cardNumberEntryMetadata ->
        saveCardNetworksMetadata(bin, cardNumberEntryMetadata)
    }

    private fun saveCardNetworksMetadata(
        bin: String,
        cardNetworksMetadata: PrimerCardNumberEntryMetadata,
    ) {
        cardMetadataCacheHelper.saveCardNetworksMetadata(
            bin.take(MAX_BIN_LENGTH),
            cardNetworksMetadata,
        )
    }

    private suspend fun emitFetchingState(cardNumber: String) =
        _metadataState.emit(
            PrimerCardMetadataState.Fetching(
                PrimerCardNumberEntryState(
                    cardNumber,
                ),
            ),
        )

    private fun updateLastInputData(lastInputData: PrimerCardData?) {
        this.lastInputData = lastInputData
    }

    private fun logCardNetworksFetchedEvent(binMetadataResult: CardBinMetadataResult) =
        analyticsRepository.addEvent(
            MessageAnalyticsParams(
                MessageType.INFO,
                "Fetched card networks: ${binMetadataResult.items}.",
                Severity.INFO,
            ),
        )

    private fun logCardNetworksFetchingErrorEvent(throwable: Throwable) =
        analyticsRepository.addEvent(
            MessageAnalyticsParams(
                MessageType.ERROR,
                "Failed to remotely validate card network: ${throwable.message}",
                Severity.ERROR,
            ),
        )

    private fun logCardNetworksLocalFallbackEvent() =
        analyticsRepository.addEvent(
            MessageAnalyticsParams(
                MessageType.INFO,
                REMOTE_VALIDATION_FAILED_MESSAGE,
                Severity.WARN,
            ),
        )

    private fun List<PrimerCardNetwork>.toCardNetworksMetadata() =
        PrimerCardNetworksMetadata(
            this,
            this.firstOrNull { primerCardNetwork -> primerCardNetwork.allowed },
        )

    /*
     * Determines if the user should be allowed to select between card networks.
     * Returns false if any of the detected card networks are in the disallowed list (e.g., EFTPOS).
     * This prevents user selection for EFTPOS co-branded cards while allowing it for EU co-badge cards.
     */
    private fun allowsUserNetworkSelection(cardNetworks: List<PrimerCardNetwork>): Boolean {
        return cardNetworks.none { it.network in USER_SELECTION_DISALLOWED_CARD_NETWORKS }
    }

    internal companion object {
        private const val MIN_SELECTABLE_NETWORKS_SIZE = 1
        private const val BIN_CALL_TIMEOUT = 10000L

        /*
         * Card networks for which user selection should be disabled.
         * For EFTPOS co-branded cards, merchants need automatic routing control
         * without exposing brand choice to customers.
         */
        private val USER_SELECTION_DISALLOWED_CARD_NETWORKS = setOf(CardNetwork.Type.EFTPOS)

        val REMOTE_VALIDATION_FAILED_MESSAGE =
            """
            Local validation was used where remote validation would have been preferred
            (max BIN length exceeded).
            """.trimIndent()
    }
}
