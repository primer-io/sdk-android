package io.primer.android.card.implementation.validation.domain

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.spyk
import io.mockk.unmockkAll
import io.primer.android.checkoutModules.domain.repository.CheckoutModuleRepository
import io.primer.android.configuration.domain.model.CheckoutModule
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.cardShared.binData.domain.CardMetadataCacheHelper
import io.primer.cardShared.validation.domain.CardCvvValidator
import io.primer.cardShared.validation.domain.CardExpiryDateValidator
import io.primer.cardShared.validation.domain.CardNumberValidator
import io.primer.cardShared.validation.domain.CardholderNameValidator
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class CardInputDataValidatorTest {

    private lateinit var checkoutModuleRepository: CheckoutModuleRepository
    private lateinit var cardInputDataValidator: CardInputDataValidator

    @BeforeEach
    fun setUp() {
        checkoutModuleRepository = mockk(relaxed = true)
        cardInputDataValidator = CardInputDataValidator(checkoutModuleRepository)

        mockkConstructor(CardExpiryDateValidator::class)
        mockkConstructor(CardholderNameValidator::class)

        DISdkContext.headlessSdkContainer = mockk<SdkContainer>(relaxed = true).also { sdkContainer ->
            val cont = spyk<DependencyContainer>().also { container ->
                container.registerFactory<CardMetadataCacheHelper> { mockk(relaxed = true) }
            }

            every { sdkContainer.containers }.returns(mutableMapOf(cont::class.simpleName.orEmpty() to cont))
        }
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `validate should return errors when card number, expiry date, and cvv are invalid`() = runTest {
        val cardData = PrimerCardData("invalid_number", "invalid_date", "invalid_cvv", "John Doe")

        coEvery {
            checkoutModuleRepository.getCardInformation()
        } returns mockk<CheckoutModule.CardInformation> {
            every { options } returns mapOf(PrimerInputElementType.ALL.field to true)
        }

        mockkConstructor(CardNumberValidator::class) {
            coEvery { anyConstructed<CardNumberValidator>().validate(any()) } returns PrimerInputValidationError(
                "invalid-card-number",
                "Card number is not valid.",
                PrimerInputElementType.CARD_NUMBER
            )
        }
        mockkConstructor(CardCvvValidator::class) {
            coEvery { anyConstructed<CardCvvValidator>().validate(any()) } returns PrimerInputValidationError(
                "invalid-cvv",
                "CVV is not valid.",
                PrimerInputElementType.CVV
            )
        }

        coEvery { CardExpiryDateValidator().validate(any()) } returns PrimerInputValidationError(
            "invalid-expiry-date",
            "Expiry date is not valid.",
            PrimerInputElementType.EXPIRY_DATE
        )
        coEvery { CardholderNameValidator().validate(any()) } returns null

        val validationErrors = cardInputDataValidator.validate(cardData)

        assertEquals(3, validationErrors.size)
        assertTrue(validationErrors.any { it.errorId == "invalid-card-number" })
        assertTrue(validationErrors.any { it.errorId == "invalid-expiry-date" })
        assertTrue(validationErrors.any { it.errorId == "invalid-cvv" })
    }

    @Test
    fun `validate should return no errors when all inputs are valid`() = runTest {
        val cardData = PrimerCardData("4111111111111111", "12/2025", "123", "John Doe")

        coEvery {
            checkoutModuleRepository.getCardInformation()
        } returns mockk<CheckoutModule.CardInformation> {
            every { options } returns mapOf(PrimerInputElementType.ALL.field to true)
        }

        mockkConstructor(CardNumberValidator::class) {
            coEvery { anyConstructed<CardNumberValidator>().validate(any()) } returns null
        }
        mockkConstructor(CardCvvValidator::class) {
            coEvery { anyConstructed<CardCvvValidator>().validate(any()) } returns null
        }

        coEvery { CardExpiryDateValidator().validate(any()) } returns null
        coEvery { CardholderNameValidator().validate(any()) } returns null

        val validationErrors = cardInputDataValidator.validate(cardData)

        assertTrue(validationErrors.isEmpty())
    }

    @Test
    fun `validate should not validate cardholder name when it is disabled`() = runTest {
        val cardData = PrimerCardData("4111111111111111", "12/2025", "123", "John Doe")

        coEvery {
            checkoutModuleRepository.getCardInformation()
        } returns mockk<CheckoutModule.CardInformation> {
            every { options } returns mapOf(
                PrimerInputElementType.ALL.field to true,
                PrimerInputElementType.CARDHOLDER_NAME.field to false
            )
        }

        mockkConstructor(CardNumberValidator::class) {
            coEvery { anyConstructed<CardNumberValidator>().validate(any()) } returns null
        }
        mockkConstructor(CardCvvValidator::class) {
            coEvery { anyConstructed<CardCvvValidator>().validate(any()) } returns null
        }
        coEvery { CardExpiryDateValidator().validate(any()) } returns null

        val validationErrors = cardInputDataValidator.validate(cardData)

        assertTrue(validationErrors.isEmpty())
    }

    @Test
    fun `validate should return errors for invalid cardholder name when it is enabled`() = runTest {
        val cardData = PrimerCardData("4111111111111111", "12/2025", "123", "")

        coEvery {
            checkoutModuleRepository.getCardInformation()
        } returns mockk<CheckoutModule.CardInformation> {
            every { options } returns mapOf(PrimerInputElementType.ALL.field to true)
        }

        mockkConstructor(CardNumberValidator::class) {
            coEvery { anyConstructed<CardNumberValidator>().validate(any()) } returns null
        }
        mockkConstructor(CardCvvValidator::class) {
            coEvery { anyConstructed<CardCvvValidator>().validate(any()) } returns null
        }

        coEvery { CardExpiryDateValidator().validate(any()) } returns null
        coEvery { CardholderNameValidator().validate(any()) } returns PrimerInputValidationError(
            "invalid-cardholder-name",
            "Cardholder name cannot be blank.",
            PrimerInputElementType.CARDHOLDER_NAME
        )

        val validationErrors = cardInputDataValidator.validate(cardData)

        assertEquals(1, validationErrors.size)
        assertTrue(validationErrors.any { it.errorId == "invalid-cardholder-name" })
    }
}
