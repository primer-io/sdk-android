package io.primer.android.components.domain.payments.metadata.card

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.Severity
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.components.domain.core.models.card.PrimerCardNetworksMetadata
import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.core.models.card.PrimerCardMetadataState
import io.primer.android.components.domain.core.models.card.PrimerCardNetwork
import io.primer.android.components.domain.core.models.card.PrimerCardNumberEntryMetadata
import io.primer.android.components.domain.core.models.card.PrimerCardNumberEntryState
import io.primer.android.components.domain.core.models.card.ValidationSource
import io.primer.android.components.domain.payments.metadata.card.CardDataMetadataStateRetriever.Companion.REMOTE_VALIDATION_FAILED_MESSAGE
import io.primer.android.components.domain.payments.metadata.card.model.CardBinMetadata
import io.primer.android.components.domain.payments.metadata.card.model.MAX_BIN_LENGTH
import io.primer.android.components.domain.payments.metadata.card.repository.CardBinMetadataRepository
import io.primer.android.components.domain.payments.paymentMethods.raw.repository.card.OrderedAllowedCardNetworksRepository
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.test.extensions.toListDuring
import io.primer.android.ui.CardNetwork
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
internal class CardDataMetadataStateRetrieverTest {

    @RelaxedMockK
    lateinit var cardBinMetadataRepository: CardBinMetadataRepository

    @RelaxedMockK
    lateinit var orderedAllowedCardNetworksRepository: OrderedAllowedCardNetworksRepository

    @RelaxedMockK
    lateinit var metadataCacheHelper: CardMetadataCacheHelper

    @RelaxedMockK
    lateinit var analyticsRepository: AnalyticsRepository

    @RelaxedMockK
    lateinit var logReporter: LogReporter

    private lateinit var metadataStateRetriever: CardDataMetadataStateRetriever

    @BeforeEach
    fun setUp() {
        metadataStateRetriever = CardDataMetadataStateRetriever(
            cardBinMetadataRepository,
            orderedAllowedCardNetworksRepository,
            metadataCacheHelper,
            analyticsRepository,
            logReporter
        )
    }

    @Test
    fun `metadataState should emit Fetching and Fetched states with remote co-badged ordered selectable card networks when getBinMetadata is successful and all cards are supported`() {
        val cardData = mockk<PrimerCardData>(relaxed = true)
        every { cardData.cardNumber } returns CARD_NUMBER

        val source = ValidationSource.REMOTE
        val cardBinMetadata = listOf(CARTES_BANCAIRES_CARD_BIN_METADATA, VISA_CARD_BIN_METADATA)

        coEvery { cardBinMetadataRepository.getBinMetadata(any(), any()) }.returns(
            Result.success(cardBinMetadata)
        )

        every { orderedAllowedCardNetworksRepository.getOrderedAllowedCardNetworks() }.returns(
            listOf(CardNetwork.Type.VISA, CardNetwork.Type.CARTES_BANCAIRES, CardNetwork.Type.AMEX)
        )

        val visaCardNetwork = PrimerCardNetwork(CardNetwork.Type.VISA, "VISA", true)
        val cbCardNetwork = PrimerCardNetwork(CardNetwork.Type.CARTES_BANCAIRES, "CB", true)

        val expectedNetworks = listOf(visaCardNetwork, cbCardNetwork)
        val expectedCardNumberEntryMetadata = PrimerCardNumberEntryMetadata(
            PrimerCardNetworksMetadata(
                expectedNetworks,
                expectedNetworks.firstOrNull()
            ),
            PrimerCardNetworksMetadata(
                expectedNetworks,
                expectedNetworks.firstOrNull()
            ),
            source
        )

        runTest {
            launch {
                metadataStateRetriever.handleInputData(cardData)
            }
            val metadataStates = metadataStateRetriever.metadataState.toListDuring(1.seconds)

            assertEquals(
                listOf(
                    PrimerCardMetadataState.Fetching(PrimerCardNumberEntryState(CARD_NUMBER)),
                    PrimerCardMetadataState.Fetched(
                        expectedCardNumberEntryMetadata,
                        PrimerCardNumberEntryState(CARD_NUMBER)
                    )
                ),
                metadataStates
            )
        }

        coVerify {
            cardBinMetadataRepository.getBinMetadata(
                CARD_NUMBER.take(MAX_BIN_LENGTH),
                source
            )
        }

        verify {
            analyticsRepository.addEvent(
                MessageAnalyticsParams(
                    MessageType.INFO,
                    "Fetched card networks: $cardBinMetadata.",
                    Severity.INFO
                )
            )
        }

        verify {
            metadataCacheHelper.saveCardNetworksMetadata(
                CARD_NUMBER.take(MAX_BIN_LENGTH),
                expectedCardNumberEntryMetadata
            )
        }
    }

    @Test
    fun `metadataState should emit Fetching and Fetched states with remote co-badged ordered filtered detected card networks only when getBinMetadata is successful and only one card network is supported`() {
        val cardData = mockk<PrimerCardData>(relaxed = true)
        every { cardData.cardNumber } returns CARD_NUMBER

        val cardBinMetadata = listOf(VISA_CARD_BIN_METADATA, CARTES_BANCAIRES_CARD_BIN_METADATA)

        coEvery { cardBinMetadataRepository.getBinMetadata(any(), any()) }.returns(
            Result.success(cardBinMetadata)
        )

        every { orderedAllowedCardNetworksRepository.getOrderedAllowedCardNetworks() }.returns(
            listOf(CardNetwork.Type.CARTES_BANCAIRES, CardNetwork.Type.AMEX)
        )

        val visaCardNetwork = PrimerCardNetwork(CardNetwork.Type.VISA, "VISA", false)
        val cbCardNetwork = PrimerCardNetwork(CardNetwork.Type.CARTES_BANCAIRES, "CB", true)

        val expectedCardNumberEntryMetadata = PrimerCardNumberEntryMetadata(
            null,
            PrimerCardNetworksMetadata(
                listOf(cbCardNetwork, visaCardNetwork),
                cbCardNetwork
            ),
            ValidationSource.REMOTE
        )

        runTest {
            launch {
                metadataStateRetriever.handleInputData(cardData)
            }
            val metadataStates = metadataStateRetriever.metadataState.toListDuring(1.seconds)

            assertEquals(
                listOf(
                    PrimerCardMetadataState.Fetching(PrimerCardNumberEntryState(CARD_NUMBER)),
                    PrimerCardMetadataState.Fetched(
                        expectedCardNumberEntryMetadata,
                        PrimerCardNumberEntryState(CARD_NUMBER)
                    )
                ),
                metadataStates
            )
        }

        coVerify {
            cardBinMetadataRepository.getBinMetadata(
                CARD_NUMBER.take(MAX_BIN_LENGTH),
                ValidationSource.REMOTE
            )
        }

        verify {
            analyticsRepository.addEvent(
                MessageAnalyticsParams(
                    MessageType.INFO,
                    "Fetched card networks: $cardBinMetadata.",
                    Severity.INFO
                )
            )
        }

        verify {
            metadataCacheHelper.saveCardNetworksMetadata(
                CARD_NUMBER.take(MAX_BIN_LENGTH),
                expectedCardNumberEntryMetadata
            )
        }
    }

    @Test
    fun `metadataState should emit Fetching and Fetched states with only detected local fallback card network when getBinMetadata failed and card networks are supported`() {
        val cardData = mockk<PrimerCardData>(relaxed = true)
        val exception = mockk<Exception>(relaxed = true)
        every { cardData.cardNumber } returns CARD_NUMBER
        coEvery {
            cardBinMetadataRepository.getBinMetadata(
                any(),
                ValidationSource.REMOTE
            )
        }.returns(
            Result.failure(exception)
        )

        coEvery {
            cardBinMetadataRepository.getBinMetadata(
                any(),
                ValidationSource.LOCAL_FALLBACK
            )
        }.returns(
            Result.success(listOf(VISA_CARD_BIN_METADATA))
        )

        every { orderedAllowedCardNetworksRepository.getOrderedAllowedCardNetworks() }.returns(
            listOf(CardNetwork.Type.VISA, CardNetwork.Type.CARTES_BANCAIRES, CardNetwork.Type.AMEX)
        )

        val visaCardNetwork =
            PrimerCardNetwork(CardNetwork.Type.VISA, "VISA", true)
        val expectedCardNumberEntryMetadata = PrimerCardNumberEntryMetadata(
            null,
            PrimerCardNetworksMetadata(listOf(visaCardNetwork), visaCardNetwork),
            ValidationSource.LOCAL_FALLBACK
        )

        runTest {
            launch {
                metadataStateRetriever.handleInputData(cardData)
            }
            val metadataStates = metadataStateRetriever.metadataState.toListDuring(1.seconds)

            assertEquals(
                listOf(
                    PrimerCardMetadataState.Fetching(PrimerCardNumberEntryState(CARD_NUMBER)),
                    PrimerCardMetadataState.Fetched(
                        expectedCardNumberEntryMetadata,
                        PrimerCardNumberEntryState(CARD_NUMBER)
                    )
                ),
                metadataStates
            )
        }

        coVerify {
            cardBinMetadataRepository.getBinMetadata(
                CARD_NUMBER.take(MAX_BIN_LENGTH),
                ValidationSource.REMOTE
            )
        }
        coVerify {
            cardBinMetadataRepository.getBinMetadata(
                CARD_NUMBER.take(MAX_BIN_LENGTH),
                ValidationSource.LOCAL_FALLBACK
            )
        }
        verify {
            analyticsRepository.addEvent(
                MessageAnalyticsParams(
                    MessageType.ERROR,
                    "Failed to remotely validate card network: ${exception.message}",
                    Severity.ERROR
                )
            )
        }
        verify {
            logReporter.warn("Remote card validation failed: ${exception.message}")
            logReporter.warn(REMOTE_VALIDATION_FAILED_MESSAGE)
        }

        verify {
            metadataCacheHelper.saveCardNetworksMetadata(
                CARD_NUMBER.take(MAX_BIN_LENGTH),
                expectedCardNumberEntryMetadata
            )
        }
    }

    @Test
    fun `metadataState should emit Fetching and Fetched states with only local detected network and without preferred network when getBinMetadata failed and card network is not supported`() {
        val cardData = mockk<PrimerCardData>(relaxed = true)
        val exception = mockk<Exception>(relaxed = true)
        every { cardData.cardNumber } returns CARD_NUMBER
        coEvery {
            cardBinMetadataRepository.getBinMetadata(
                any(),
                ValidationSource.REMOTE
            )
        }.returns(
            Result.failure(exception)
        )

        coEvery {
            cardBinMetadataRepository.getBinMetadata(
                any(),
                ValidationSource.LOCAL_FALLBACK
            )
        }.returns(
            Result.success(listOf(VISA_CARD_BIN_METADATA))
        )

        every { orderedAllowedCardNetworksRepository.getOrderedAllowedCardNetworks() }.returns(
            listOf(CardNetwork.Type.CARTES_BANCAIRES, CardNetwork.Type.AMEX)
        )

        val visaCardNetwork =
            PrimerCardNetwork(CardNetwork.Type.VISA, "VISA", false)
        val expectedCardNumberEntryMetadata = PrimerCardNumberEntryMetadata(
            null,
            PrimerCardNetworksMetadata(listOf(visaCardNetwork), null),
            ValidationSource.LOCAL_FALLBACK
        )

        runTest {
            launch {
                metadataStateRetriever.handleInputData(cardData)
            }

            val metadataStates = metadataStateRetriever.metadataState.toListDuring(1.seconds)

            assertEquals(
                listOf(
                    PrimerCardMetadataState.Fetching(PrimerCardNumberEntryState(CARD_NUMBER)),
                    PrimerCardMetadataState.Fetched(
                        expectedCardNumberEntryMetadata,
                        PrimerCardNumberEntryState(CARD_NUMBER)
                    )
                ),
                metadataStates
            )
        }

        coVerify {
            cardBinMetadataRepository.getBinMetadata(
                CARD_NUMBER.take(MAX_BIN_LENGTH),
                ValidationSource.REMOTE
            )
        }

        coVerify {
            cardBinMetadataRepository.getBinMetadata(
                CARD_NUMBER.take(MAX_BIN_LENGTH),
                ValidationSource.LOCAL_FALLBACK
            )
        }
        verify {
            analyticsRepository.addEvent(
                MessageAnalyticsParams(
                    MessageType.ERROR,
                    "Failed to remotely validate card network: ${exception.message}",
                    Severity.ERROR
                )
            )
        }
        verify {
            logReporter.warn(REMOTE_VALIDATION_FAILED_MESSAGE)
        }

        verify {
            metadataCacheHelper.saveCardNetworksMetadata(
                CARD_NUMBER.take(MAX_BIN_LENGTH),
                expectedCardNumberEntryMetadata
            )
        }
    }

    @Test
    fun `metadataState should emit Fetched state with only detected local card network when card number is less than 8 digits and card network is supported`() {
        val cardData = mockk<PrimerCardData>(relaxed = true)
        every { cardData.cardNumber } returns CARD_NUMBER_SHORT

        coEvery {
            cardBinMetadataRepository.getBinMetadata(
                any(),
                ValidationSource.LOCAL
            )
        }.returns(
            Result.success(listOf(VISA_CARD_BIN_METADATA))
        )

        every { orderedAllowedCardNetworksRepository.getOrderedAllowedCardNetworks() }.returns(
            listOf(CardNetwork.Type.VISA, CardNetwork.Type.CARTES_BANCAIRES, CardNetwork.Type.AMEX)
        )

        val visaCardNetwork =
            PrimerCardNetwork(CardNetwork.Type.VISA, "VISA", true)
        val expectedCardNumberEntryMetadata = PrimerCardNumberEntryMetadata(
            null,
            PrimerCardNetworksMetadata(listOf(visaCardNetwork), visaCardNetwork),
            ValidationSource.LOCAL
        )

        runTest {
            launch {
                metadataStateRetriever.handleInputData(cardData)
            }
            val metadataStates = metadataStateRetriever.metadataState.toListDuring(1.seconds)

            assertEquals(
                listOf(
                    PrimerCardMetadataState.Fetched(
                        expectedCardNumberEntryMetadata,
                        PrimerCardNumberEntryState(CARD_NUMBER_SHORT)
                    )
                ),
                metadataStates
            )

            assertEquals(cardData, metadataStateRetriever.lastInputData)
        }

        coVerify {
            cardBinMetadataRepository.getBinMetadata(CARD_NUMBER_SHORT, ValidationSource.LOCAL)
        }

        verify(exactly = 0) {
            analyticsRepository.addEvent(any())
        }

        verify {
            metadataCacheHelper.saveCardNetworksMetadata(
                CARD_NUMBER_SHORT,
                expectedCardNumberEntryMetadata
            )
        }
    }

    @Test
    fun `metadataState should emit Fetched state with detected local card network and without preferred network when card number is less than 8 digits and card network is not supported`() {
        val cardData = mockk<PrimerCardData>(relaxed = true)
        every { cardData.cardNumber } returns CARD_NUMBER_SHORT

        coEvery {
            cardBinMetadataRepository.getBinMetadata(
                any(),
                ValidationSource.LOCAL
            )
        }.returns(
            Result.success(listOf(VISA_CARD_BIN_METADATA))
        )

        every { orderedAllowedCardNetworksRepository.getOrderedAllowedCardNetworks() }.returns(
            listOf(CardNetwork.Type.CARTES_BANCAIRES, CardNetwork.Type.AMEX)
        )

        val visaCardNetwork =
            PrimerCardNetwork(CardNetwork.Type.VISA, "VISA", false)
        val expectedCardNumberEntryMetadata = PrimerCardNumberEntryMetadata(
            null,
            PrimerCardNetworksMetadata(listOf(visaCardNetwork), null),
            ValidationSource.LOCAL
        )

        runTest {
            launch {
                metadataStateRetriever.handleInputData(cardData)
            }
            val metadataStates = metadataStateRetriever.metadataState.toListDuring(1.seconds)

            assertEquals(
                listOf(
                    PrimerCardMetadataState.Fetched(
                        expectedCardNumberEntryMetadata,
                        PrimerCardNumberEntryState(CARD_NUMBER_SHORT)
                    )
                ),
                metadataStates
            )

            assertEquals(cardData, metadataStateRetriever.lastInputData)
        }

        coVerify {
            cardBinMetadataRepository.getBinMetadata(CARD_NUMBER_SHORT, ValidationSource.LOCAL)
        }

        verify {
            metadataCacheHelper.saveCardNetworksMetadata(
                CARD_NUMBER_SHORT,
                expectedCardNumberEntryMetadata
            )
        }
    }

    @Test
    fun `metadataState should emit Fetched state with empty detected networks and without preferred network when card number is blank`() {
        val cardData = mockk<PrimerCardData>(relaxed = true)
        every { cardData.cardNumber } returns "  "

        every { orderedAllowedCardNetworksRepository.getOrderedAllowedCardNetworks() }.returns(
            listOf(CardNetwork.Type.CARTES_BANCAIRES, CardNetwork.Type.AMEX)
        )

        runTest {
            launch {
                metadataStateRetriever.handleInputData(cardData)
            }
            val metadataStates = metadataStateRetriever.metadataState.toListDuring(1.seconds)

            assertEquals(
                listOf(
                    PrimerCardMetadataState.Fetched(
                        PrimerCardNumberEntryMetadata(
                            null,
                            PrimerCardNetworksMetadata(emptyList(), null),
                            ValidationSource.LOCAL
                        ),
                        PrimerCardNumberEntryState("")
                    )
                ),
                metadataStates
            )

            assertEquals(cardData, metadataStateRetriever.lastInputData)
        }
    }

    @Test
    fun `metadataState should not emit states when called with same bin`() {
        val cardData = mockk<PrimerCardData>(relaxed = true)
        every { cardData.cardNumber } returns CARD_NUMBER

        runTest {
            metadataStateRetriever.lastInputData = cardData
            launch {
                metadataStateRetriever.handleInputData(cardData)
            }
            val metadataStates = metadataStateRetriever.metadataState.toListDuring(1.seconds)
            assertEquals(emptyList(), metadataStates)

            assertEquals(cardData, metadataStateRetriever.lastInputData)
        }

        coVerify(exactly = 0) {
            cardBinMetadataRepository.getBinMetadata(CARD_NUMBER_SHORT, any())
        }

        verify(exactly = 0) { metadataCacheHelper.saveCardNetworksMetadata(any(), any()) }
    }

    private companion object {

        const val CARD_NUMBER = "40355000000"
        const val CARD_NUMBER_SHORT = "40355"

        val VISA_CARD_BIN_METADATA = CardBinMetadata("VISA", CardNetwork.Type.VISA)
        val CARTES_BANCAIRES_CARD_BIN_METADATA =
            CardBinMetadata("CB", CardNetwork.Type.CARTES_BANCAIRES)
    }
}
