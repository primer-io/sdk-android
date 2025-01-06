package io.primer.android.viewmodel

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.core.models.card.PrimerCardNetwork
import io.primer.android.components.domain.core.models.card.PrimerCardNetworksMetadata
import io.primer.android.components.domain.core.models.card.PrimerCardNumberEntryMetadata
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManagerInterface
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManagerListener
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.model.SyncValidationError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CardViewModelTest {

    private lateinit var mockCardManager: PrimerHeadlessUniversalCheckoutRawDataManagerInterface
    private var capturedListenerSlot = slot<PrimerHeadlessUniversalCheckoutRawDataManagerListener>()

    private lateinit var viewModel: CardViewModel

    @BeforeEach
    fun setup() {
        mockCardManager = mockk {
            every { setRawData(any()) } just Runs
            every { setListener(capture(capturedListenerSlot)) } just Runs
            every { getRequiredInputElementTypes() } returns listOf(PrimerInputElementType.CARD_NUMBER)
        }
        viewModel = CardViewModel(mockCardManager)
    }

    @Test
    fun `initialize sets up card manager listener`() {
        viewModel.initialize()
        verify { mockCardManager.setListener(any()) }
    }

    @Test
    fun `onCardDataChanged updates card data correctly`() {
        val cardData = PrimerCardData(
            cardNumber = "4111111111111111",
            cardHolderName = "John Doe",
            expiryDate = "12/25",
            cvv = "123"
        )

        viewModel.onCardDataChanged(cardData)

        verify { mockCardManager.setRawData(cardData) }
    }

    @Test
    fun `setSelectedNetwork updates card network state`() {
        val initialCardData = PrimerCardData(
            cardNumber = "4111111111111111",
            cardHolderName = "John Doe",
            expiryDate = "12/25",
            cvv = "123",
            cardNetwork = CardNetwork.Type.VISA
        )
        val selectedNetwork = CardNetwork.Type.CARTES_BANCAIRES

        viewModel.onCardDataChanged(initialCardData)
        viewModel.setSelectedNetwork(selectedNetwork)

        val networkState = viewModel.cardNetworksState.value

        assertEquals(selectedNetwork, networkState.selectedNetwork)
        verify(exactly = 1) { mockCardManager.setRawData(initialCardData) }
        verify(exactly = 1) { mockCardManager.setRawData(initialCardData.copy(cardNetwork = selectedNetwork)) }
    }

    @Test
    fun `isValid should return true when all conditions are met`() {
        // Arrange
        viewModel.updateValidationErrors(emptyList())
        viewModel.onCardDataChanged(PrimerCardData("1234", "12/23", "123", "John Doe"))
        viewModel.tokenizationStatus.value = TokenizationStatus.NONE

        // Act
        val result = viewModel.isValid()

        // Assert
        assertTrue(result)
    }

    @Test
    fun `isValid should return false when card validation errors are not empty`() {
        // Arrange
        viewModel.initialize()
        viewModel.updateValidationErrors(emptyList())
        viewModel.onCardDataChanged(PrimerCardData("1234", "12/23", "123", "John Doe"))
        viewModel.tokenizationStatus.value = TokenizationStatus.NONE

        val error = PrimerInputValidationError("invalid-card-number", "2", PrimerInputElementType.CARD_NUMBER)

        capturedListenerSlot.captured.onValidationChanged(false, listOf(error))

        // Act
        val result = viewModel.isValid()

        // Assert
        assertFalse(result)
    }

    @Test
    fun `isValid should return false when cachedCardData is null`() {
        // Arrange
        viewModel.tokenizationStatus.value = TokenizationStatus.NONE

        // Act
        val result = viewModel.isValid()

        // Assert
        assertFalse(result)
    }

    @Test
    fun `isValid should return false when billing address validation errors are not empty`() {
        // Arrange
        viewModel.updateValidationErrors(listOf(SyncValidationError(PrimerInputElementType.ADDRESS_LINE_1, "error", 1)))
        viewModel.onCardDataChanged(PrimerCardData("1234", "12/23", "123", "John Doe"))
        viewModel.tokenizationStatus.value = TokenizationStatus.NONE

        // Act
        val result = viewModel.isValid()

        // Assert
        assertFalse(result)
    }

    @Test
    fun `isValid should return false when isSubmitButtonEnabled returns false`() {
        // Arrange
        viewModel.onCardDataChanged(PrimerCardData("1234", "12/23", "123", "John Doe"))
        viewModel.tokenizationStatus.value = TokenizationStatus.NONE
        viewModel.isMetadataUpdating = true

        // Act
        val result = viewModel.isValid()

        // Assert
        assertFalse(result)
    }

    @Test
    fun `isSubmitButtonEnabled should return true when tokenizationStatus is NONE and isMetadataUpdating is false`() {
        // Arrange
        viewModel.tokenizationStatus.value = TokenizationStatus.NONE
        viewModel.isMetadataUpdating = false

        // Act
        val result = viewModel.isSubmitButtonEnabled(viewModel.tokenizationStatus.value)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `isSubmitButtonEnabled should return true when tokenizationStatus is ERROR and isMetadataUpdating is false`() {
        // Arrange
        viewModel.tokenizationStatus.value = TokenizationStatus.ERROR
        viewModel.isMetadataUpdating = false

        // Act
        val result = viewModel.isSubmitButtonEnabled(viewModel.tokenizationStatus.value)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `isSubmitButtonEnabled should return false when tokenizationStatus is NONE and isMetadataUpdating is true`() {
        // Arrange
        viewModel.tokenizationStatus.value = TokenizationStatus.NONE
        viewModel.isMetadataUpdating = true

        // Act
        val result = viewModel.isSubmitButtonEnabled(viewModel.tokenizationStatus.value)

        // Assert
        assertFalse(result)
    }

    @Test
    fun `isSubmitButtonEnabled should return false when tokenizationStatus is ERROR and isMetadataUpdating is true`() {
        // Arrange
        viewModel.tokenizationStatus.value = TokenizationStatus.ERROR
        viewModel.isMetadataUpdating = true

        // Act
        val result = viewModel.isSubmitButtonEnabled(viewModel.tokenizationStatus.value)

        // Assert
        assertFalse(result)
    }

    @Test
    fun `isSubmitButtonEnabled should return false when tokenizationStatus is neither NONE nor ERROR`() {
        // Arrange
        viewModel.tokenizationStatus.value = TokenizationStatus.LOADING
        viewModel.isMetadataUpdating = false

        // Act
        val result = viewModel.isSubmitButtonEnabled(viewModel.tokenizationStatus.value)

        // Assert
        assertFalse(result)
    }

    @Test
    fun `submit sets submitted flag to true`() {
        every { mockCardManager.submit() } answers { nothing }

        viewModel.submit()

        verify(exactly = 1) { mockCardManager.submit() }
        assertTrue(viewModel.submitted)
    }

    @Test
    fun `getValidAutoFocusableFields should return all fields when there are no errors`() {
        // Arrange
        every { mockCardManager.getRequiredInputElementTypes() } returns emptyList()

        val errors = emptyList<PrimerInputValidationError>()

        // Act
        val result = viewModel.getValidAutoFocusableFields(errors)

        // Assert
        assertEquals(
            setOf(
                PrimerInputElementType.CARD_NUMBER,
                PrimerInputElementType.CVV,
                PrimerInputElementType.EXPIRY_DATE
            ),
            result
        )
    }

    @Test
    fun `getValidAutoFocusableFields should exclude CARD_NUMBER when CARD_NUMBER error exists`() {
        // Arrange
        every { mockCardManager.getRequiredInputElementTypes() } returns emptyList()
        val errors = listOf(
            PrimerInputValidationError("", "", PrimerInputElementType.CARD_NUMBER)
        )

        // Act
        val result = viewModel.getValidAutoFocusableFields(errors)

        // Assert
        assertEquals(
            setOf(
                PrimerInputElementType.CVV,
                PrimerInputElementType.EXPIRY_DATE
            ),
            result
        )
    }

    @Test
    fun `getValidAutoFocusableFields should exclude CVV when CVV error exists`() {
        // Arrange
        every { mockCardManager.getRequiredInputElementTypes() } returns emptyList()

        val errors = listOf(
            PrimerInputValidationError("", "", PrimerInputElementType.CVV)
        )

        // Act
        val result = viewModel.getValidAutoFocusableFields(errors)

        // Assert
        assertEquals(
            setOf(
                PrimerInputElementType.CARD_NUMBER,
                PrimerInputElementType.EXPIRY_DATE
            ),
            result
        )
    }

    @Test
    fun `getValidAutoFocusableFields should exclude EXPIRY_DATE when EXPIRY_DATE error exists`() {
        // Arrange
        every { mockCardManager.getRequiredInputElementTypes() } returns emptyList()

        val errors = listOf(
            PrimerInputValidationError("", "", PrimerInputElementType.EXPIRY_DATE)
        )

        // Act
        val result = viewModel.getValidAutoFocusableFields(errors)

        // Assert
        assertEquals(
            setOf(
                PrimerInputElementType.CARD_NUMBER,
                PrimerInputElementType.CVV
            ),
            result
        )
    }

    @Test
    fun `getValidAutoFocusableFields should add CARDHOLDER_NAME if it is required and not blank`() {
        // Arrange
        every { mockCardManager.getRequiredInputElementTypes() } returns listOf(PrimerInputElementType.CARDHOLDER_NAME)

        viewModel.onCardDataChanged(PrimerCardData("", "", "", cardHolderName = "John Doe"))

        val errors = emptyList<PrimerInputValidationError>()

        // Act
        val result = viewModel.getValidAutoFocusableFields(errors)

        // Assert
        assertEquals(
            setOf(
                PrimerInputElementType.CARD_NUMBER,
                PrimerInputElementType.CVV,
                PrimerInputElementType.EXPIRY_DATE,
                PrimerInputElementType.CARDHOLDER_NAME
            ),
            result
        )
    }

    @Test
    fun `getValidAutoFocusableFields should not add CARDHOLDER_NAME if it is required but blank`() {
        // Arrange
        every { mockCardManager.getRequiredInputElementTypes() } returns listOf(PrimerInputElementType.CARDHOLDER_NAME)

        viewModel.onCardDataChanged(PrimerCardData("", "", "", cardHolderName = ""))
        val errors = emptyList<PrimerInputValidationError>()

        // Act
        val result = viewModel.getValidAutoFocusableFields(errors)

        // Assert
        assertEquals(
            setOf(
                PrimerInputElementType.CARD_NUMBER,
                PrimerInputElementType.CVV,
                PrimerInputElementType.EXPIRY_DATE
            ),
            result
        )
    }

    @Test
    fun `getValidAutoFocusableFields should not add CARDHOLDER_NAME if card data is empty`() {
        // Arrange
        every { mockCardManager.getRequiredInputElementTypes() } returns listOf(PrimerInputElementType.CARDHOLDER_NAME)

        val errors = emptyList<PrimerInputValidationError>()

        // Act
        val result = viewModel.getValidAutoFocusableFields(errors)

        // Assert
        assertEquals(
            setOf(
                PrimerInputElementType.CARD_NUMBER,
                PrimerInputElementType.CVV,
                PrimerInputElementType.EXPIRY_DATE
            ),
            result
        )
    }

    @Test
    fun `getValidAutoFocusableFields should not add CARDHOLDER_NAME if it is required but null`() {
        // Arrange
        every { mockCardManager.getRequiredInputElementTypes() } returns listOf(PrimerInputElementType.CARDHOLDER_NAME)

        viewModel.onCardDataChanged(PrimerCardData("", "", "", cardHolderName = null))
        val errors = emptyList<PrimerInputValidationError>()

        // Act
        val result = viewModel.getValidAutoFocusableFields(errors)

        // Assert
        assertEquals(
            setOf(
                PrimerInputElementType.CARD_NUMBER,
                PrimerInputElementType.CVV,
                PrimerInputElementType.EXPIRY_DATE
            ),
            result
        )
    }

    @Test
    fun `getValidAutoFocusableFields should not add CARDHOLDER_NAME if it is not required`() {
        // Arrange
        every { mockCardManager.getRequiredInputElementTypes() } returns emptyList()

        val errors = emptyList<PrimerInputValidationError>()

        viewModel.onCardDataChanged(PrimerCardData("", "", "", cardHolderName = "John Doe"))

        // Act
        val result = viewModel.getValidAutoFocusableFields(errors)

        // Assert
        assertEquals(
            setOf(
                PrimerInputElementType.CARD_NUMBER,
                PrimerInputElementType.CVV,
                PrimerInputElementType.EXPIRY_DATE
            ),
            result
        )
    }

    @Test
    fun `handleFetchedMetadata sets correct card networks state`() {
        val visa = PrimerCardNetwork(network = CardNetwork.Type.VISA, CardNetwork.Type.VISA.displayName, true)
        val master = PrimerCardNetwork(
            network = CardNetwork.Type.MASTERCARD,
            displayName = CardNetwork.Type.MASTERCARD.displayName,
            allowed = true
        )
        val mockMetadata = mockk<PrimerCardNumberEntryMetadata> {
            every { selectableCardNetworks } returns PrimerCardNetworksMetadata(
                items = listOf(visa, master),
                preferred = visa
            )
            every { detectedCardNetworks } returns PrimerCardNetworksMetadata(
                items = listOf(visa),
                preferred = null
            )
        }

        // Act
        viewModel.handleFetchedMetadata(mockMetadata)

        // Assert
        val networkState = viewModel.cardNetworksState.value
        assertEquals(2, networkState.networks.size)
        assertEquals(CardNetwork.Type.VISA, networkState.preferredNetwork)
        assertEquals(CardNetwork.Type.VISA, networkState.selectedNetwork)
    }
}
