package io.primer.cardShared.validation.domain

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.components.domain.core.models.card.PrimerCardNetwork
import io.primer.android.components.domain.core.models.card.PrimerCardNetworksMetadata
import io.primer.android.components.domain.core.models.card.PrimerCardNumberEntryMetadata
import io.primer.android.components.domain.core.models.card.ValidationSource
import io.primer.cardShared.binData.domain.CardMetadataCacheHelper
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CardNumberValidatorTest {

    @RelaxedMockK
    internal lateinit var metadataCacheHelper: CardMetadataCacheHelper

    private lateinit var validator: CardNumberValidator

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        validator = CardNumberValidator(metadataCacheHelper)
    }

    @Test
    fun `validate should not return error when card number is valid and VISA is supported and source is REMOTE`() = runTest {
        val primerCardNumberEntryMetadata = mockk<PrimerCardNumberEntryMetadata>(relaxed = true)
        val primerCardNetworksMetadata = mockk<PrimerCardNetworksMetadata>(relaxed = true)

        every { primerCardNumberEntryMetadata.detectedCardNetworks }.returns(
            primerCardNetworksMetadata
        )
        every { primerCardNumberEntryMetadata.source }.returns(ValidationSource.REMOTE)
        every { primerCardNetworksMetadata.items }.returns(listOf(ALLOWED_VISA_CARD_NETWORK))
        every { metadataCacheHelper.getCardNetworksMetadata(any()) }.returns(
            primerCardNumberEntryMetadata
        )

        val resultError = validator.run {
            validate("4242424242424242")
        }
        assertEquals(null, resultError)
    }

    @Test
    fun `validate should not return error when card number is valid and VISA is supported and source is LOCAL_FALLBACK`() = runTest {
        val primerCardNumberEntryMetadata = mockk<PrimerCardNumberEntryMetadata>(relaxed = true)
        val primerCardNetworksMetadata = mockk<PrimerCardNetworksMetadata>(relaxed = true)

        every { primerCardNumberEntryMetadata.detectedCardNetworks }.returns(
            primerCardNetworksMetadata
        )
        every { primerCardNumberEntryMetadata.source }.returns(ValidationSource.LOCAL_FALLBACK)
        every { primerCardNetworksMetadata.items }.returns(listOf(ALLOWED_VISA_CARD_NETWORK))
        every { metadataCacheHelper.getCardNetworksMetadata(any()) }.returns(
            primerCardNumberEntryMetadata
        )

        val resultError = validator.run {
            validate("4242424242424242")
        }
        assertEquals(null, resultError)
    }

    @Test
    fun `validate should not return error when card number is valid source is LOCAL`() = runTest {
        val primerCardNumberEntryMetadata = mockk<PrimerCardNumberEntryMetadata>(relaxed = true)
        every { primerCardNumberEntryMetadata.source }.returns(ValidationSource.LOCAL)
        every { metadataCacheHelper.getCardNetworksMetadata(any()) }.returns(
            primerCardNumberEntryMetadata
        )

        val resultError = validator.run {
            validate("4242424242424242")
        }
        assertEquals(null, resultError)
    }

    @Test
    fun `validate should return error 'invalid-card-type' when card number is valid and VISA is not supported and source is REMOTE`() = runTest {
        val primerCardNumberEntryMetadata = mockk<PrimerCardNumberEntryMetadata>(relaxed = true)
        val primerCardNetworksMetadata = mockk<PrimerCardNetworksMetadata>(relaxed = true)

        every { primerCardNumberEntryMetadata.detectedCardNetworks }.returns(
            primerCardNetworksMetadata
        )
        every { primerCardNumberEntryMetadata.source }.returns(ValidationSource.REMOTE)
        every { primerCardNetworksMetadata.items }.returns(listOf(UNALLOWED_VISA_CARD_NETWORK))
        every { metadataCacheHelper.getCardNetworksMetadata(any()) }.returns(
            primerCardNumberEntryMetadata
        )
        val resultError = validator.run {
            validate("4242424242424242")
        }
        assertEquals(UNSUPPORTED_CARD_TYPE_ERROR_ID, resultError?.errorId)
        assertEquals("Unsupported card type detected: Visa", resultError?.description)
    }

    @Test
    fun `validate should return error 'invalid-card-type' when card number is valid and VISA is not supported and source is LOCAL_FALLBACK`() = runTest {
        val primerCardNumberEntryMetadata = mockk<PrimerCardNumberEntryMetadata>(relaxed = true)
        val primerCardNetworksMetadata = mockk<PrimerCardNetworksMetadata>(relaxed = true)

        every { primerCardNumberEntryMetadata.detectedCardNetworks }.returns(
            primerCardNetworksMetadata
        )
        every { primerCardNumberEntryMetadata.source }.returns(ValidationSource.REMOTE)
        every { primerCardNetworksMetadata.items }.returns(listOf(UNALLOWED_VISA_CARD_NETWORK))
        every { metadataCacheHelper.getCardNetworksMetadata(any()) }.returns(
            primerCardNumberEntryMetadata
        )
        val resultError = validator.run {
            validate("4242424242424242")
        }
        assertEquals(UNSUPPORTED_CARD_TYPE_ERROR_ID, resultError?.errorId)
        assertEquals("Unsupported card type detected: Visa", resultError?.description)
    }

    @Test
    fun `validate should return error 'invalid-card-number' when card number is blank`() = runTest {
        val resultError = validator.run {
            validate("")
        }
        assertEquals("invalid-card-number", resultError?.errorId)
    }

    @Test
    fun `validate should return error 'invalid-card-number' when card number is null`() = runTest {
        val resultError = validator.run {
            validate(null)
        }
        assertEquals("invalid-card-number", resultError?.errorId)
    }

    @Test
    fun `validate should return error 'invalid-card-number' when card number is invalid and card network is supported and source is REMOTE`() = runTest {
        val primerCardNumberEntryMetadata = mockk<PrimerCardNumberEntryMetadata>(relaxed = true)
        val primerCardNetworksMetadata = mockk<PrimerCardNetworksMetadata>(relaxed = true)

        every { primerCardNumberEntryMetadata.detectedCardNetworks }.returns(
            primerCardNetworksMetadata
        )
        every { primerCardNumberEntryMetadata.source }.returns(ValidationSource.REMOTE)
        every { primerCardNetworksMetadata.items }.returns(listOf(ALLOWED_VISA_CARD_NETWORK))
        every { metadataCacheHelper.getCardNetworksMetadata(any()) }.returns(
            primerCardNumberEntryMetadata
        )
        val resultError = validator.run {
            validate("4242424242424")
        }
        assertEquals("invalid-card-number", resultError?.errorId)
    }

    @Test
    fun `validate should return error 'invalid-card-number' when card number is invalid and card network is supported and source is LOCAL_FALLBACK`() = runTest {
        val primerCardNumberEntryMetadata = mockk<PrimerCardNumberEntryMetadata>(relaxed = true)
        val primerCardNetworksMetadata = mockk<PrimerCardNetworksMetadata>(relaxed = true)

        every { primerCardNumberEntryMetadata.detectedCardNetworks }.returns(
            primerCardNetworksMetadata
        )
        every { primerCardNumberEntryMetadata.source }.returns(ValidationSource.LOCAL_FALLBACK)
        every { primerCardNetworksMetadata.items }.returns(listOf(ALLOWED_VISA_CARD_NETWORK))
        every { metadataCacheHelper.getCardNetworksMetadata(any()) }.returns(
            primerCardNumberEntryMetadata
        )
        val resultError = validator.run {
            validate("4242424242424")
        }
        assertEquals("invalid-card-number", resultError?.errorId)
    }

    @Test
    fun `validate should return error 'invalid-card-number' when metadata source is LOCAL`() = runTest {
        val primerCardNumberEntryMetadata = mockk<PrimerCardNumberEntryMetadata>(relaxed = true)
        every { primerCardNumberEntryMetadata.source }.returns(ValidationSource.LOCAL)
        val resultError = validator.run {
            validate("424242")
        }
        assertEquals("invalid-card-number", resultError?.errorId)
    }

    @Test
    fun `validate should return error 'invalid-card-number' when metadata cache does not contain value for a given bin`() = runTest {
        every { metadataCacheHelper.getCardNetworksMetadata(any()) }.returns(null)
        val resultError = validator.run {
            validate("4242424242424")
        }
        assertEquals("invalid-card-number", resultError?.errorId)
    }

    @Test
    fun `validate should return error 'invalid-card-number' when metadata cache does contain empty value for a given bin`() = runTest {
        val primerCardNumberEntryMetadata = mockk<PrimerCardNumberEntryMetadata>(relaxed = true)
        val primerCardNetworksMetadata = mockk<PrimerCardNetworksMetadata>(relaxed = true)

        every { primerCardNumberEntryMetadata.detectedCardNetworks }.returns(
            primerCardNetworksMetadata
        )
        every { primerCardNumberEntryMetadata.source }.returns(ValidationSource.REMOTE)
        every { primerCardNetworksMetadata.items }.returns(emptyList())
        every { metadataCacheHelper.getCardNetworksMetadata(any()) }.returns(
            primerCardNumberEntryMetadata
        )
        val resultError = validator.run {
            validate("4242424242424")
        }
        assertEquals("invalid-card-number", resultError?.errorId)
    }

    private companion object {
        val ALLOWED_VISA_CARD_NETWORK = PrimerCardNetwork(CardNetwork.Type.VISA, "Visa", true)
        val UNALLOWED_VISA_CARD_NETWORK = PrimerCardNetwork(CardNetwork.Type.VISA, "Visa", false)
        const val UNSUPPORTED_CARD_TYPE_ERROR_ID = "unsupported-card-type"
    }
}
