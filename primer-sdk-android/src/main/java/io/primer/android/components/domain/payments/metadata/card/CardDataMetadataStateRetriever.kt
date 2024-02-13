package io.primer.android.components.domain.payments.metadata.card

import androidx.annotation.VisibleForTesting
import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.Severity
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.components.domain.core.models.card.PrimerCardNetworksMetadata
import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.core.models.card.PrimerCardMetadataState
import io.primer.android.components.domain.core.models.card.PrimerCardNumberEntryMetadata
import io.primer.android.components.domain.core.models.card.PrimerCardNetwork
import io.primer.android.components.domain.core.models.card.PrimerCardNumberEntryState
import io.primer.android.components.domain.core.models.card.ValidationSource
import io.primer.android.components.domain.payments.metadata.PaymentRawDataMetadataStateRetriever
import io.primer.android.components.domain.payments.metadata.card.model.CardBinMetadata
import io.primer.android.components.domain.payments.metadata.card.model.MAX_BIN_LENGTH
import io.primer.android.components.domain.payments.metadata.card.model.toSortedPrimerCardNetworks
import io.primer.android.components.domain.payments.metadata.card.repository.CardBinMetadataRepository
import io.primer.android.components.domain.payments.paymentMethods.raw.repository.card.OrderedAllowedCardNetworksRepository
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.extensions.mapSuspendCatching
import io.primer.android.utils.sanitizedCardNumber
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withTimeout

internal class CardDataMetadataStateRetriever(
    private val binMetadataDataRepository: CardBinMetadataRepository,
    private val allowedCardNetworksRepository: OrderedAllowedCardNetworksRepository,
    private val cardMetadataCacheHelper: CardMetadataCacheHelper,
    private val analyticsRepository: AnalyticsRepository,
    private val logReporter: LogReporter
) : PaymentRawDataMetadataStateRetriever<PrimerCardData, PrimerCardMetadataState> {

    @VisibleForTesting
    internal var lastInputData: PrimerCardData? = null

    private val _cardMetadataState = MutableSharedFlow<PrimerCardMetadataState>()
    override val metadataState = _cardMetadataState.distinctUntilChanged()

    override suspend fun handleInputData(
        inputData: PrimerCardData
    ) {
        val newCardNumber = inputData.cardNumber.sanitizedCardNumber()
        val lastCardNumber = lastInputData?.cardNumber?.sanitizedCardNumber()
        if (lastCardNumber?.take(MAX_BIN_LENGTH) != newCardNumber.take(MAX_BIN_LENGTH)) {
            updateLastInputData(null)
            when {
                newCardNumber.length >= MAX_BIN_LENGTH -> getRemoteCardMetadata(newCardNumber)
                    .also { updateLastInputData(inputData) }

                else -> _cardMetadataState.emit(
                    PrimerCardMetadataState.Fetched(
                        getLocalCardMetadata(newCardNumber, ValidationSource.LOCAL),
                        PrimerCardNumberEntryState(newCardNumber)
                    )
                ).also {
                    updateLastInputData(inputData)
                }
            }
        }
    }

    private suspend fun getRemoteCardMetadata(cardNumber: String) = withTimeout(
        BIN_CALL_TIMEOUT
    ) {
        emitFetchingState(cardNumber)
        binMetadataDataRepository.getBinMetadata(
            cardNumber.take(MAX_BIN_LENGTH),
            ValidationSource.REMOTE
        ).mapSuspendCatching { binMetadata ->
            val orderedAllowedCardNetworks =
                allowedCardNetworksRepository.getOrderedAllowedCardNetworks()
            val allNetworks = binMetadata.toSortedPrimerCardNetworks(orderedAllowedCardNetworks)
            val allowedNetworks =
                allNetworks.filter { primerCardNetwork -> primerCardNetwork.allowed }
            logCardNetworksFetchedEvent(binMetadata)
            PrimerCardNumberEntryMetadata(
                allowedNetworks.takeIf { primerCardNetworks ->
                    primerCardNetworks.size > MIN_SELECTABLE_NETWORKS_SIZE
                }?.toCardNetworksMetadata(),
                allNetworks.toCardNetworksMetadata(),
                ValidationSource.REMOTE
            )
        }.recoverCatching { throwable ->
            logReporter.warn("Remote card validation failed: ${throwable.message}")
            logCardNetworksFetchingErrorEvent(throwable)
            getLocalCardMetadata(cardNumber.take(MAX_BIN_LENGTH), ValidationSource.LOCAL_FALLBACK)
        }.onSuccess { cardNumberEntryMetadata ->
            saveCardNetworksMetadata(
                cardNumber.take(MAX_BIN_LENGTH),
                cardNumberEntryMetadata
            )
            _cardMetadataState.emit(
                PrimerCardMetadataState.Fetched(
                    cardNumberEntryMetadata,
                    PrimerCardNumberEntryState(
                        cardNumber
                    )
                )
            )
        }
    }

    private suspend fun getLocalCardMetadata(
        bin: String,
        source: ValidationSource
    ) = when (bin.isBlank()) {
        true -> PrimerCardNumberEntryMetadata(
            null,
            emptyList<PrimerCardNetwork>().toCardNetworksMetadata(),
            source
        )

        false -> binMetadataDataRepository.getBinMetadata(bin, source).getOrThrow()
            .toSortedPrimerCardNetworks(
                allowedCardNetworksRepository.getOrderedAllowedCardNetworks()
            )
            .let { primerCardNetworks ->
                PrimerCardNumberEntryMetadata(
                    null,
                    primerCardNetworks.toCardNetworksMetadata(),
                    source
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
        cardNetworksMetadata: PrimerCardNumberEntryMetadata
    ) {
        cardMetadataCacheHelper.saveCardNetworksMetadata(
            bin.take(MAX_BIN_LENGTH),
            cardNetworksMetadata
        )
    }

    private suspend fun emitFetchingState(cardNumber: String) = _cardMetadataState.emit(
        PrimerCardMetadataState.Fetching(
            PrimerCardNumberEntryState(
                cardNumber
            )
        )
    )

    private fun updateLastInputData(lastInputData: PrimerCardData?) {
        this.lastInputData = lastInputData
    }

    private fun logCardNetworksFetchedEvent(binMetadata: List<CardBinMetadata>) =
        analyticsRepository.addEvent(
            MessageAnalyticsParams(
                MessageType.INFO,
                "Fetched card networks: $binMetadata.",
                Severity.INFO
            )
        )

    private fun logCardNetworksFetchingErrorEvent(throwable: Throwable) =
        analyticsRepository.addEvent(
            MessageAnalyticsParams(
                MessageType.ERROR,
                "Failed to remotely validate card network: ${throwable.message}",
                Severity.ERROR
            )
        )

    private fun logCardNetworksLocalFallbackEvent() =
        analyticsRepository.addEvent(
            MessageAnalyticsParams(
                MessageType.INFO,
                REMOTE_VALIDATION_FAILED_MESSAGE,
                Severity.WARN
            )
        )

    private fun List<PrimerCardNetwork>.toCardNetworksMetadata() =
        PrimerCardNetworksMetadata(
            this,
            this.firstOrNull { primerCardNetwork -> primerCardNetwork.allowed }
        )

    internal companion object {

        private const val MIN_SELECTABLE_NETWORKS_SIZE = 1
        private const val BIN_CALL_TIMEOUT = 10000L
        val REMOTE_VALIDATION_FAILED_MESSAGE = """
            Local validation was used where remote validation would have been preferred
            (max BIN length exceeded).
        """.trimIndent()
    }
}
