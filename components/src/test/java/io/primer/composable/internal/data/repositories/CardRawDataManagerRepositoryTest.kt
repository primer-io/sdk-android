package io.primer.composable.internal.data.repositories

import io.mockk.Ordering
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadataState
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManager
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManagerInterface
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManagerListener
import io.primer.android.internal.data.repositories.CardRawDataManagerRepository
import io.primer.android.internal.domain.repositories.RawDataManagerRepository
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CardRawDataManagerRepositoryTest {

    private lateinit var repository: RawDataManagerRepository
    private lateinit var mockCardManager: PrimerHeadlessUniversalCheckoutRawDataManagerInterface
    private val listenerSlot = slot<PrimerHeadlessUniversalCheckoutRawDataManagerListener>()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())

        mockCardManager = mockk(relaxed = true)

        every {
            mockCardManager.setListener(capture(listenerSlot))
        } just runs

        mockkObject(PrimerHeadlessUniversalCheckoutRawDataManager)
        every {
            PrimerHeadlessUniversalCheckoutRawDataManager.newInstance(PaymentMethodType.PAYMENT_CARD.name)
        } returns mockCardManager

        repository = CardRawDataManagerRepository()
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(PrimerHeadlessUniversalCheckoutRawDataManager)
        Dispatchers.resetMain()
    }

    @Nested
    inner class InitializationTests {

        @Test
        fun `should set listener on card manager`() = runTest {
            // Access a flow to trigger the lazy initialization
            val job = launch {
                repository.validationState.take(1).toList()
            }
            advanceUntilIdle()
            job.cancel()

            verify(exactly = 1) {
                mockCardManager.setListener(any())
            }
        }
    }

    @Nested
    inner class RequiredInputElementTypesTests {

        @Test
        fun `should return required input element types from card manager`() {
            val expectedTypes = listOf(
                PrimerInputElementType.CARDHOLDER_NAME,
                PrimerInputElementType.CARD_NUMBER,
                PrimerInputElementType.EXPIRY_DATE,
                PrimerInputElementType.CVV,
            )

            every { mockCardManager.getRequiredInputElementTypes() } returns expectedTypes

            val result = repository.getRequiredInputElementTypes()

            assertEquals(expectedTypes, result)
            verify(exactly = 1) { mockCardManager.getRequiredInputElementTypes() }
        }

        @Test
        fun `should handle empty required input element types`() {
            every { mockCardManager.getRequiredInputElementTypes() } returns emptyList()

            val result = repository.getRequiredInputElementTypes()

            assertTrue(result.isEmpty())
            verify(exactly = 1) { mockCardManager.getRequiredInputElementTypes() }
        }
    }

    @Nested
    inner class ValidationStateTests {

        @Test
        fun `should emit validation errors when validation changes to invalid`() = runTest {
            val expectedErrors = listOf(
                createMockValidationError("CARD_NUMBER", "Invalid card number"),
                createMockValidationError("CVV", "CVV is required"),
            )

            // Start collecting
            val job = launch {
                val result = repository.validationState.first()
                assertEquals(expectedErrors, result)
            }

            // Wait for flow to be ready
            advanceUntilIdle()

            // Trigger validation change
            listenerSlot.captured.onValidationChanged(
                isValid = false,
                errors = expectedErrors,
            )

            // Process the event
            advanceUntilIdle()

            // Complete the test
            job.join()
        }

        @Test
        fun `should emit empty errors when validation changes to valid`() = runTest {
            val job = launch {
                val result = repository.validationState.first()
                assertTrue(result.isEmpty())
            }

            advanceUntilIdle()

            // Trigger validation change with valid state
            listenerSlot.captured.onValidationChanged(
                isValid = true,
                errors = emptyList(),
            )

            advanceUntilIdle()
            job.join()
        }

        @Test
        fun `should emit multiple validation state changes`() = runTest {
            val errors1 = listOf(createMockValidationError("CARD_NUMBER", "Required"))
            val errors2 = listOf(createMockValidationError("CVV", "Invalid"))

            val job = launch {
                val results = repository.validationState.take(2).toList()
                assertEquals(2, results.size)
                assertEquals(errors1, results[0])
                assertEquals(errors2, results[1])
            }

            advanceUntilIdle()

            // Emit multiple validation changes
            listenerSlot.captured.onValidationChanged(false, errors1)
            listenerSlot.captured.onValidationChanged(false, errors2)

            advanceUntilIdle()
            job.join()
        }

        @Test
        fun `should handle validation with isValid true but with errors`() = runTest {
            val expectedErrors = listOf(createMockValidationError("CARD_NUMBER", "Warning"))

            val job = launch {
                val result = repository.validationState.first()
                // Should still emit the errors even if isValid is true
                assertEquals(expectedErrors, result)
            }

            advanceUntilIdle()

            // Edge case: isValid = true but errors present
            listenerSlot.captured.onValidationChanged(
                isValid = true,
                errors = expectedErrors,
            )

            advanceUntilIdle()
            job.join()
        }

        @Test
        fun `should handle validation with isValid false but no errors`() = runTest {
            val job = launch {
                val result = repository.validationState.first()
                // Should emit empty list even if isValid is false
                assertTrue(result.isEmpty())
            }

            advanceUntilIdle()

            // Edge case: isValid = false but no errors
            listenerSlot.captured.onValidationChanged(
                isValid = false,
                errors = emptyList(),
            )

            advanceUntilIdle()
            job.join()
        }
    }

    @Nested
    inner class MetadataStateTests {

        @Test
        fun `should emit metadata state changes`() = runTest {
            val expectedState = mockk<PrimerPaymentMethodMetadataState>()

            val job = launch {
                val result = repository.metadataState.first()
                assertEquals(expectedState, result)
            }

            advanceUntilIdle()

            // Trigger metadata state change
            listenerSlot.captured.onMetadataStateChanged(expectedState)

            advanceUntilIdle()
            job.join()
        }

        @Test
        fun `should emit multiple metadata state changes`() = runTest {
            val state1 = mockk<PrimerPaymentMethodMetadataState>()
            val state2 = mockk<PrimerPaymentMethodMetadataState>()

            val job = launch {
                val results = repository.metadataState.take(2).toList()
                assertEquals(2, results.size)
                assertEquals(state1, results[0])
                assertEquals(state2, results[1])
            }

            advanceUntilIdle()

            // Emit multiple metadata changes
            listenerSlot.captured.onMetadataStateChanged(state1)
            listenerSlot.captured.onMetadataStateChanged(state2)

            advanceUntilIdle()
            job.join()
        }
    }

    @Nested
    inner class DataOperationTests {

        @Test
        fun `should delegate setData to card manager`() {
            val cardData = createMockCardData()

            repository.setData(cardData)

            verify(exactly = 1) { mockCardManager.setRawData(cardData) }
        }

        @Test
        fun `should delegate submit to card manager`() {
            repository.submit()

            verify(exactly = 1) { mockCardManager.submit() }
        }

        @Test
        fun `should call setData and submit in correct order`() {
            val cardData = createMockCardData()

            repository.setData(cardData)
            repository.submit()

            verify(ordering = Ordering.SEQUENCE) {
                PrimerHeadlessUniversalCheckoutRawDataManager.newInstance(PaymentMethodType.PAYMENT_CARD.name)
                mockCardManager.setRawData(cardData)
                mockCardManager.submit()
            }
        }
    }

    @Nested
    inner class FlowSharingTests {

        @Test
        fun `should share validation state between multiple collectors`() = runTest {
            val expectedErrors = listOf(createMockValidationError("CARD_NUMBER", "Invalid"))

            // Start two collectors
            val job1 = launch {
                val result = repository.validationState.first()
                assertEquals(expectedErrors, result)
            }
            val job2 = launch {
                val result = repository.validationState.first()
                assertEquals(expectedErrors, result)
            }

            advanceUntilIdle()

            // Emit once
            listenerSlot.captured.onValidationChanged(false, expectedErrors)

            advanceUntilIdle()

            // Both collectors should receive the same event
            job1.join()
            job2.join()
        }

        @Test
        fun `should share metadata state between multiple collectors`() = runTest {
            val expectedState = mockk<PrimerPaymentMethodMetadataState>()

            // Start two collectors
            val job1 = launch {
                val result = repository.metadataState.first()
                assertEquals(expectedState, result)
            }
            val job2 = launch {
                val result = repository.metadataState.first()
                assertEquals(expectedState, result)
            }

            advanceUntilIdle()

            // Emit once
            listenerSlot.captured.onMetadataStateChanged(expectedState)

            advanceUntilIdle()

            // Both collectors should receive the same event
            job1.join()
            job2.join()
        }
    }

    @Nested
    inner class MixedEventTests {

        @Test
        fun `validation state should only receive validation events`() = runTest {
            val expectedErrors = listOf(createMockValidationError("CVV", "Required"))
            val metadataState = mockk<PrimerPaymentMethodMetadataState>()

            val job = launch {
                val result = repository.validationState.first()
                assertEquals(expectedErrors, result)
            }

            advanceUntilIdle()

            // Emit metadata event first (should be filtered out)
            listenerSlot.captured.onMetadataStateChanged(metadataState)
            // Then emit validation event
            listenerSlot.captured.onValidationChanged(false, expectedErrors)

            advanceUntilIdle()
            job.join()
        }

        @Test
        fun `metadata state should only receive metadata events`() = runTest {
            val validationErrors = listOf(createMockValidationError("CVV", "Required"))
            val expectedMetadata = mockk<PrimerPaymentMethodMetadataState>()

            val job = launch {
                val result = repository.metadataState.first()
                assertEquals(expectedMetadata, result)
            }

            advanceUntilIdle()

            // Emit validation event first (should be filtered out)
            listenerSlot.captured.onValidationChanged(false, validationErrors)
            // Then emit metadata event
            listenerSlot.captured.onMetadataStateChanged(expectedMetadata)

            advanceUntilIdle()
            job.join()
        }
    }

    // Helper methods
    private fun createMockValidationError(
        elementType: String,
        message: String,
    ): PrimerInputValidationError = mockk {
        every { inputElementType } returns mockk {
            every { name } returns elementType
        }
        every { description } returns message
    }

    private fun createMockCardData(): PrimerCardData = mockk()
}
