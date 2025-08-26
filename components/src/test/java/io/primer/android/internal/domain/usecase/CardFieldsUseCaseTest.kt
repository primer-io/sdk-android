package io.primer.android.internal.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.configuration.domain.CachePolicy
import io.primer.android.configuration.domain.ConfigurationInteractor
import io.primer.android.configuration.domain.model.CheckoutModule
import io.primer.android.configuration.domain.model.Configuration
import io.primer.android.configuration.domain.model.ConfigurationParams
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.internal.domain.repositories.RawDataManagerRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CardFieldsUseCaseTest {

    private lateinit var useCase: CardFieldsUseCase
    private lateinit var mockRawDataManagerRepository: RawDataManagerRepository
    private lateinit var mockConfigurationInteractor: ConfigurationInteractor
    private lateinit var mockLogReporter: LogReporter
    private val validationStateFlow = MutableStateFlow<List<PrimerInputValidationError>>(emptyList())

    @BeforeEach
    fun setUp() {
        mockRawDataManagerRepository = mockk()
        mockConfigurationInteractor = mockk()
        mockLogReporter = mockk(relaxed = true)

        every { mockRawDataManagerRepository.validationState } returns validationStateFlow
        every { mockRawDataManagerRepository.setData(any()) } just runs

        useCase = CardFieldsUseCase(
            mockRawDataManagerRepository,
            mockConfigurationInteractor,
            mockLogReporter,
        )
    }

    @Nested
    inner class FormDataTests {
        @Test
        fun `updateField should update form data and repository`() = runTest {
            // Given
            val field = PrimerInputElementType.CARD_NUMBER
            val value = "4111111111111111"

            // When
            useCase.updateField(field, value)

            // Then
            val formData = useCase.formData.first()
            assertEquals(value, formData[field])

            verify(exactly = 1) {
                mockRawDataManagerRepository.setData(
                    PrimerCardData(
                        cardNumber = value,
                        expiryDate = "",
                        cvv = "",
                        cardNetwork = CardNetwork.Type.OTHER,
                    ),
                )
            }
        }

        @Test
        fun `updateField should accumulate multiple field updates`() = runTest {
            // When
            useCase.updateField(PrimerInputElementType.CARD_NUMBER, "4111111111111111")
            useCase.updateField(PrimerInputElementType.CVV, "123")
            useCase.updateField(PrimerInputElementType.EXPIRY_DATE, "12/25")

            // Then
            val formData = useCase.formData.first()
            assertEquals("4111111111111111", formData[PrimerInputElementType.CARD_NUMBER])
            assertEquals("123", formData[PrimerInputElementType.CVV])
            assertEquals("12/25", formData[PrimerInputElementType.EXPIRY_DATE])

            verify(exactly = 3) { mockRawDataManagerRepository.setData(any()) }
        }
    }

    @Nested
    inner class CardNetworkTests {
        @Test
        fun `updateCardNetwork should update network and repository`() = runTest {
            // Given
            val network = CardNetwork.Type.VISA

            // When
            useCase.updateCardNetwork(network)

            // Then
            verify(exactly = 1) {
                mockRawDataManagerRepository.setData(
                    PrimerCardData(
                        cardNumber = "",
                        expiryDate = "",
                        cvv = "",
                        cardNetwork = network,
                    ),
                )
            }
        }
    }

    @Nested
    inner class ValidationTests {
        @Test
        fun `validationErrors should be empty before submit attempted`() = runTest {
            // Given
            val error = mockk<PrimerInputValidationError>()
            validationStateFlow.value = listOf(error)

            // When
            val errors = useCase.validationErrors.first()

            // Then
            assertTrue(errors.isEmpty())
        }

        @Test
        fun `isSubmitAllowed should return true when no validation errors`() = runTest {
            // Given
            validationStateFlow.value = emptyList()

            // When
            val result = useCase.isSubmitAllowed()

            // Then
            assertTrue(result)
        }

        @Test
        fun `isSubmitAllowed should return false when validation errors exist`() = runTest {
            // Given
            val error = mockk<PrimerInputValidationError>()
            validationStateFlow.value = listOf(error)

            // When
            val result = useCase.isSubmitAllowed()

            // Then
            assertFalse(result)
        }

        @Test
        fun `markSubmitAttempted should update submit state and repository`() = runTest {
            // When
            useCase.markSubmitAttempted()

            // Then
            verify(exactly = 1) { mockRawDataManagerRepository.setData(any()) }
        }
    }

    @Nested
    inner class FieldConfigurationTests {
        @Test
        fun `getCardFields should return filtered required input types`() {
            // Given
            val requiredTypes = listOf(
                PrimerInputElementType.CARD_NUMBER,
                PrimerInputElementType.CVV,
                PrimerInputElementType.EXPIRY_DATE,
                PrimerInputElementType.FIRST_NAME, // Not a card field
                PrimerInputElementType.LAST_NAME, // Not a card field
            )
            every { mockRawDataManagerRepository.getRequiredInputElementTypes() } returns requiredTypes

            // When
            val cardFields = useCase.getCardFields()

            // Then
            assertEquals(3, cardFields.size)
            assertTrue(cardFields.contains(PrimerInputElementType.CARD_NUMBER))
            assertTrue(cardFields.contains(PrimerInputElementType.CVV))
            assertTrue(cardFields.contains(PrimerInputElementType.EXPIRY_DATE))
            assertFalse(cardFields.contains(PrimerInputElementType.FIRST_NAME))

            verify(exactly = 1) { mockRawDataManagerRepository.getRequiredInputElementTypes() }
        }

        @Test
        fun `getBillingFields should return enabled billing fields from configuration`() = runTest {
            // Given
            val billingAddress = CheckoutModule.BillingAddress(
                options = mapOf(
                    PrimerInputElementType.FIRST_NAME.field to true,
                    PrimerInputElementType.LAST_NAME.field to true,
                    PrimerInputElementType.CITY.field to true,
                    PrimerInputElementType.POSTAL_CODE.field to false,
                ),
            )
            val configuration = mockk<Configuration> {
                every { checkoutModules } returns listOf(billingAddress)
            }
            coEvery {
                mockConfigurationInteractor(ConfigurationParams(CachePolicy.ForceCache))
            } returns Result.success(configuration)

            // When
            val billingFields = useCase.getBillingFields()

            // Then
            assertEquals(3, billingFields.size)
            assertTrue(billingFields.contains(PrimerInputElementType.FIRST_NAME))
            assertTrue(billingFields.contains(PrimerInputElementType.LAST_NAME))
            assertTrue(billingFields.contains(PrimerInputElementType.CITY))
            assertFalse(billingFields.contains(PrimerInputElementType.POSTAL_CODE))

            coVerify(exactly = 1) { mockConfigurationInteractor(ConfigurationParams(CachePolicy.ForceCache)) }
        }

        @Test
        fun `getBillingFields should return empty list when configuration fails`() = runTest {
            // Given
            val exception = Exception("Configuration error")
            coEvery {
                mockConfigurationInteractor(ConfigurationParams(CachePolicy.ForceCache))
            } returns Result.failure(exception)

            // When
            val billingFields = useCase.getBillingFields()

            // Then
            assertTrue(billingFields.isEmpty())

            verify(exactly = 1) { mockLogReporter.error("Failed to getBillingFields: Configuration error") }
            coVerify(exactly = 1) { mockConfigurationInteractor(ConfigurationParams(CachePolicy.ForceCache)) }
        }
    }
}
