package io.primer.cardShared.validation.domain

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.primer.android.components.domain.core.models.card.PrimerCardNetwork
import io.primer.android.components.domain.core.models.card.PrimerCardNetworksMetadata
import io.primer.android.components.domain.core.models.card.PrimerCardNumberEntryMetadata
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.cardShared.binData.domain.CardMetadataCacheHelper
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CardCvvValidatorTest {
    @RelaxedMockK
    internal lateinit var metadataCacheHelper: CardMetadataCacheHelper

    private lateinit var validator: CardCvvValidator

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        validator = CardCvvValidator(metadataCacheHelper)
    }

    @Test
    fun `validate should not return error when CVV is valid and locally resolved card is VISA`() =
        runTest {
            val resultError =
                validator.run {
                    validate(CvvData("333", "4242424242424242"))
                }
            assertEquals(null, resultError)
        }

    @Test
    fun `validate should not return error when CVV is valid and remotely resolved card is VISA`() =
        runTest {
            val primerCardNumberEntryMetadata = mockk<PrimerCardNumberEntryMetadata>(relaxed = true)
            val primerCardNetworksMetadata = mockk<PrimerCardNetworksMetadata>(relaxed = true)
            val primerCardNetwork = mockk<PrimerCardNetwork>(relaxed = true)

            every { primerCardNetwork.network }.returns(CardNetwork.Type.VISA)
            every { primerCardNumberEntryMetadata.detectedCardNetworks }.returns(
                primerCardNetworksMetadata,
            )
            every { primerCardNetworksMetadata.items }.returns(listOf(primerCardNetwork))
            every { metadataCacheHelper.getCardNetworksMetadata(any()) }.returns(
                primerCardNumberEntryMetadata,
            )
            val resultError =
                validator.run {
                    validate(CvvData("333", "4242424242424242"))
                }
            assertEquals(null, resultError)
        }

    @Test
    fun `validate should not return error when CVV is valid and remotely resolved card is CARTES_BANCAIRES`() =
        runTest {
            val primerCardNumberEntryMetadata = mockk<PrimerCardNumberEntryMetadata>(relaxed = true)
            val primerCardNetworksMetadata = mockk<PrimerCardNetworksMetadata>(relaxed = true)
            val primerCardNetwork = mockk<PrimerCardNetwork>(relaxed = true)

            every { primerCardNetwork.network }.returns(CardNetwork.Type.CARTES_BANCAIRES)
            every { primerCardNumberEntryMetadata.detectedCardNetworks }.returns(
                primerCardNetworksMetadata,
            )
            every { primerCardNetworksMetadata.items }.returns(listOf(primerCardNetwork))
            every { metadataCacheHelper.getCardNetworksMetadata(any()) }.returns(
                primerCardNumberEntryMetadata,
            )
            val resultError =
                validator.run {
                    validate(CvvData("333", "4242424242424242"))
                }
            assertEquals(null, resultError)
        }

    @Test
    fun `validate should return error 'invalid-cvv' when CVV data is null`() =
        runTest {
            val resultError =
                validator.run {
                    validate(null)
                }
            assertEquals("invalid-cvv", resultError?.errorId)
        }

    @Test
    fun `validate should return error 'invalid-cvv' when CVV is blank`() =
        runTest {
            val resultError =
                validator.run {
                    validate(CvvData("", "4242424242424242"))
                }
            assertEquals("invalid-cvv", resultError?.errorId)
        }

    @Test
    fun `validate should return error 'invalid-cvv' when CVV size is not correct`() =
        runTest {
            val resultError =
                validator.run {
                    validate(CvvData("23", "4242424242424242"))
                }
            assertEquals("invalid-cvv", resultError?.errorId)
        }

    @Test
    fun `validate should return error 'invalid-cvv' when CVV has incorrect characters`() =
        runTest {
            val resultError =
                validator.run {
                    validate(CvvData("23A", "4242424242424242"))
                }
            assertEquals("invalid-cvv", resultError?.errorId)
        }
}
