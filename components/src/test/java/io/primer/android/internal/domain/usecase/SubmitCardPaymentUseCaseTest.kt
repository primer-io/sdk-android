package io.primer.android.internal.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.primer.android.clientSessionActions.domain.ActionInteractor
import io.primer.android.clientSessionActions.domain.models.ActionUpdateBillingAddressParams
import io.primer.android.clientSessionActions.domain.models.MultipleActionUpdateParams
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.internal.domain.repositories.HeadlessRepository
import io.primer.android.internal.domain.repositories.RawDataManagerRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SubmitCardPaymentUseCaseTest {

    private lateinit var useCase: SubmitCardPaymentUseCase
    private lateinit var mockRawDataManagerRepository: RawDataManagerRepository
    private lateinit var mockHeadlessRepository: HeadlessRepository
    private lateinit var mockActionInteractor: ActionInteractor

    private val paymentResultsFlow = MutableStateFlow<Result<PrimerCheckoutData>>(
        Result.success(mockk()),
    )

    @BeforeEach
    fun setUp() {
        mockRawDataManagerRepository = mockk(relaxed = true)
        mockHeadlessRepository = mockk()
        mockActionInteractor = mockk()

        every { mockHeadlessRepository.paymentResults } returns paymentResultsFlow

        useCase = SubmitCardPaymentUseCase(
            rawDataManagerRepository = mockRawDataManagerRepository,
            headlessRepository = mockHeadlessRepository,
            actionInteractor = mockActionInteractor,
        )
    }

    private fun createFormData(vararg pairs: Pair<PrimerInputElementType, String>) =
        mapOf(*pairs)

    private fun createBillingAddressData() = mapOf(
        PrimerInputElementType.FIRST_NAME to "John",
        PrimerInputElementType.LAST_NAME to "Doe",
        PrimerInputElementType.ADDRESS_LINE_1 to "123 Main St",
        PrimerInputElementType.ADDRESS_LINE_2 to "Apt 4",
        PrimerInputElementType.CITY to "New York",
        PrimerInputElementType.POSTAL_CODE to "10001",
        PrimerInputElementType.COUNTRY_CODE to "US",
        PrimerInputElementType.STATE to "NY",
    )

    @Test
    fun `invoke should submit payment without billing address when no billing fields are provided`() = runTest {
        // Given
        val formData = emptyMap<PrimerInputElementType, String>()
        val expectedResult = mockk<PrimerCheckoutData>()
        paymentResultsFlow.value = Result.success(expectedResult)

        // When
        val result = useCase(formData)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.getOrNull())
        coVerify(exactly = 1) { mockRawDataManagerRepository.submit() }
        coVerify(exactly = 0) { mockActionInteractor.invoke(any()) }
    }

    @Test
    fun `invoke should validate and submit with full billing address`() = runTest {
        // Given
        val formData = createBillingAddressData()
        val expectedResult = mockk<PrimerCheckoutData>()
        paymentResultsFlow.value = Result.success(expectedResult)

        coEvery { mockActionInteractor.invoke(any()) } returns Result.success(mockk())

        // When
        val result = useCase(formData)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.getOrNull())

        coVerify(exactly = 1) { mockRawDataManagerRepository.submit() }
        coVerify(exactly = 1) {
            mockActionInteractor.invoke(
                withArg { actionParams ->
                    val multipleParams = actionParams as MultipleActionUpdateParams
                    val billingAction = multipleParams.params.first() as ActionUpdateBillingAddressParams
                    assertEquals("John", billingAction.firstName)
                    assertEquals("Doe", billingAction.lastName)
                    assertEquals("123 Main St", billingAction.addressLine1)
                    assertEquals("Apt 4", billingAction.addressLine2)
                    assertEquals("New York", billingAction.city)
                    assertEquals("10001", billingAction.postalCode)
                    assertEquals("US", billingAction.countryCode)
                    assertEquals("NY", billingAction.state)
                },
            )
        }
    }

    @Test
    fun `invoke should validate with partial billing address`() = runTest {
        // Given - only some billing fields filled
        val formData = createFormData(
            PrimerInputElementType.FIRST_NAME to "John",
            PrimerInputElementType.CITY to "New York",
            PrimerInputElementType.COUNTRY_CODE to "US",
        )
        val expectedResult = mockk<PrimerCheckoutData>()
        paymentResultsFlow.value = Result.success(expectedResult)

        coEvery { mockActionInteractor.invoke(any()) } returns Result.success(mockk())

        // When
        val result = useCase(formData)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.getOrNull())

        coVerify(exactly = 1) { mockRawDataManagerRepository.submit() }
        coVerify(exactly = 1) {
            mockActionInteractor.invoke(
                withArg { actionParams ->
                    val multipleParams = actionParams as MultipleActionUpdateParams
                    val billingAction = multipleParams.params.first() as ActionUpdateBillingAddressParams
                    assertEquals("John", billingAction.firstName)
                    assertEquals(null, billingAction.lastName)
                    assertEquals(null, billingAction.addressLine1)
                    assertEquals(null, billingAction.addressLine2)
                    assertEquals("New York", billingAction.city)
                    assertEquals(null, billingAction.postalCode)
                    assertEquals("US", billingAction.countryCode)
                    assertEquals(null, billingAction.state)
                },
            )
        }
    }

    @Test
    fun `invoke should skip validation when all billing fields are blank`() = runTest {
        // Given - billing fields with only blank values
        val formData = createFormData(
            PrimerInputElementType.FIRST_NAME to "",
            PrimerInputElementType.LAST_NAME to "   ",
            PrimerInputElementType.CITY to "",
        )
        val expectedResult = mockk<PrimerCheckoutData>()
        paymentResultsFlow.value = Result.success(expectedResult)

        // When
        val result = useCase(formData)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.getOrNull())
        coVerify(exactly = 1) { mockRawDataManagerRepository.submit() }
        coVerify(exactly = 0) { mockActionInteractor.invoke(any()) }
    }

    @Test
    fun `invoke should return failure when billing address validation fails`() = runTest {
        // Given
        val formData = createFormData(
            PrimerInputElementType.FIRST_NAME to "John",
            PrimerInputElementType.CITY to "New York",
        )
        val validationError = Exception("Invalid billing address")

        coEvery { mockActionInteractor.invoke(any()) } returns Result.failure(validationError)

        // When
        val result = useCase(formData)

        // Then
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertNotNull(error)
        assertTrue(error?.message?.contains("Billing address validation failed") == true)
        assertTrue(error?.message?.contains("Invalid billing address") == true)

        coVerify(exactly = 0) { mockRawDataManagerRepository.submit() }
        coVerify(exactly = 1) { mockActionInteractor.invoke(any()) }
    }

    @Test
    fun `invoke should return payment failure when submission fails`() = runTest {
        // Given
        val formData = emptyMap<PrimerInputElementType, String>()
        val paymentError = Exception("Payment failed")
        paymentResultsFlow.value = Result.failure(paymentError)

        // When
        val result = useCase(formData)

        // Then
        assertTrue(result.isFailure)
        assertEquals(paymentError, result.exceptionOrNull())
        coVerify(exactly = 1) { mockRawDataManagerRepository.submit() }
    }

    @Test
    fun `invoke should handle mixed blank and non-blank billing fields`() = runTest {
        // Given - mix of blank, whitespace, and valid values
        val formData = mapOf(
            PrimerInputElementType.FIRST_NAME to "John",
            PrimerInputElementType.LAST_NAME to "",
            PrimerInputElementType.ADDRESS_LINE_1 to "   ",
            PrimerInputElementType.CITY to "New York",
            PrimerInputElementType.POSTAL_CODE to "",
            PrimerInputElementType.COUNTRY_CODE to "US",
        )
        val expectedResult = mockk<PrimerCheckoutData>()
        paymentResultsFlow.value = Result.success(expectedResult)

        coEvery { mockActionInteractor.invoke(any()) } returns Result.success(mockk())

        // When
        val result = useCase(formData)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.getOrNull())

        coVerify(exactly = 1) {
            mockActionInteractor.invoke(
                withArg { actionParams ->
                    val multipleParams = actionParams as MultipleActionUpdateParams
                    val billingAction = multipleParams.params.first() as ActionUpdateBillingAddressParams
                    assertEquals("John", billingAction.firstName)
                    assertEquals(null, billingAction.lastName) // blank should be filtered
                    assertEquals(null, billingAction.addressLine1) // whitespace should be filtered
                    assertEquals("New York", billingAction.city)
                    assertEquals(null, billingAction.postalCode) // empty should be filtered
                    assertEquals("US", billingAction.countryCode)
                },
            )
        }
    }

    @Test
    fun `invoke should handle form data with non-billing fields`() = runTest {
        // Given - mix of billing and non-billing fields
        val formData = mapOf(
            PrimerInputElementType.CARD_NUMBER to "4111111111111111",
            PrimerInputElementType.EXPIRY_DATE to "12/25",
            PrimerInputElementType.CVV to "123",
            PrimerInputElementType.FIRST_NAME to "John",
            PrimerInputElementType.CITY to "New York",
        )
        val expectedResult = mockk<PrimerCheckoutData>()
        paymentResultsFlow.value = Result.success(expectedResult)

        coEvery { mockActionInteractor.invoke(any()) } returns Result.success(mockk())

        // When
        val result = useCase(formData)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.getOrNull())

        // Should only validate billing fields, not card fields
        coVerify(exactly = 1) {
            mockActionInteractor.invoke(
                withArg { actionParams ->
                    val multipleParams = actionParams as MultipleActionUpdateParams
                    val billingAction = multipleParams.params.first() as ActionUpdateBillingAddressParams
                    assertEquals("John", billingAction.firstName)
                    assertEquals("New York", billingAction.city)
                    // Card fields should not be included
                    assertEquals(null, billingAction.lastName)
                    assertEquals(null, billingAction.addressLine1)
                },
            )
        }
    }
}
