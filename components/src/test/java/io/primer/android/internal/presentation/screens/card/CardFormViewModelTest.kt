package io.primer.android.internal.presentation.screens.card

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import io.primer.android.api.components.card.PrimerCardFormController
import io.primer.android.components.analytics.data.model.EventType
import io.primer.android.components.analytics.data.repository.ComponentsEventsRepository
import io.primer.android.components.domain.core.models.card.PrimerCardNetwork
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.core.InstantExecutorExtension
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.internal.domain.usecase.CardFieldsUseCase
import io.primer.android.internal.domain.usecase.CardFormCleanupUseCase
import io.primer.android.internal.domain.usecase.CardNetworkUseCase
import io.primer.android.internal.domain.usecase.SetVaultOnSuccessUseCase
import io.primer.android.internal.domain.usecase.SubmitCardPaymentUseCase
import io.primer.android.internal.navigation.CheckoutResultHandler
import io.primer.android.internal.navigation.CountryNavigator
import io.primer.android.ui.core.model.SyncValidationError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantExecutorExtension::class)
class CardFormViewModelTest {

    private lateinit var cardFieldsUseCase: CardFieldsUseCase
    private lateinit var cardNetworkUseCase: CardNetworkUseCase
    private lateinit var submitCardPaymentUseCase: SubmitCardPaymentUseCase
    private lateinit var setVaultOnSuccessUseCase: SetVaultOnSuccessUseCase
    private lateinit var cleanupUseCase: CardFormCleanupUseCase
    private lateinit var logReporter: LogReporter
    private lateinit var componentsEventsRepository: ComponentsEventsRepository
    private lateinit var countryNavigator: CountryNavigator
    private lateinit var resultHandler: CheckoutResultHandler
    private lateinit var viewModel: CardFormViewModel

    private val formDataFlow = MutableStateFlow(emptyMap<PrimerInputElementType, String>())
    private val validationErrorsFlow = MutableStateFlow<List<SyncValidationError>>(emptyList())
    private val networkSelectionFlow = MutableStateFlow(PrimerCardFormController.NetworkSelection())

    @BeforeEach
    fun setup() {
        cardFieldsUseCase = mockk(relaxed = true)
        cardNetworkUseCase = mockk(relaxed = true)
        submitCardPaymentUseCase = mockk()
        setVaultOnSuccessUseCase = mockk(relaxed = true)
        cleanupUseCase = mockk(relaxed = true)
        logReporter = mockk(relaxed = true)
        componentsEventsRepository = mockk(relaxed = true)
        countryNavigator = mockk(relaxed = true)
        resultHandler = mockk(relaxed = true)

        every { cardFieldsUseCase.formData } returns formDataFlow
        every { cardFieldsUseCase.validationErrors } returns validationErrorsFlow
        every { cardFieldsUseCase.fieldFocusStates } returns flowOf(emptyMap())
        every { cardFieldsUseCase.isFormValid } returns flowOf(false)
        every { cardNetworkUseCase.networkSelection } returns networkSelectionFlow
        every { countryNavigator.countrySelectionResult } returns MutableSharedFlow()
        coEvery { cardFieldsUseCase.getCardFields() } returns listOf(
            PrimerInputElementType.CARD_NUMBER,
            PrimerInputElementType.EXPIRY_DATE,
            PrimerInputElementType.CVV,
        )
        coEvery { cardFieldsUseCase.getBillingFields() } returns listOf(
            PrimerInputElementType.POSTAL_CODE,
            PrimerInputElementType.COUNTRY_CODE,
        )

        viewModel = CardFormViewModel(
            cardFieldsUseCase = cardFieldsUseCase,
            cardNetworkUseCase = cardNetworkUseCase,
            submitCardPaymentUseCase = submitCardPaymentUseCase,
            setVaultOnSuccessUseCase = setVaultOnSuccessUseCase,
            cleanupUseCase = cleanupUseCase,
            logReporter = logReporter,
            componentsEventsRepository = componentsEventsRepository,
            countryNavigator = countryNavigator,
            resultHandler = resultHandler,
        )
    }

    @Test
    fun `should initialize state with card and billing fields`() = runTest {
        advanceUntilIdle()

        val state = viewModel.state.first()
        val expectedCardFields = listOf(
            PrimerInputElementType.CARD_NUMBER,
            PrimerInputElementType.EXPIRY_DATE,
            PrimerInputElementType.CVV,
        )
        val expectedBillingFields = listOf(
            PrimerInputElementType.POSTAL_CODE,
            PrimerInputElementType.COUNTRY_CODE,
        )
        assertEquals(expectedCardFields, state.cardFields)
        assertEquals(expectedBillingFields, state.billingFields)
    }

    @Test
    fun `updateCardNumber should update field`() = runTest {
        val cardNumber = "4111111111111111"
        viewModel.updateCardNumber(cardNumber)

        verify(exactly = 1) { cardFieldsUseCase.updateField(PrimerInputElementType.CARD_NUMBER, cardNumber) }
    }

    @Test
    fun `updateCvv should update CVV field`() = runTest {
        val cvv = "123"
        viewModel.updateCvv(cvv)

        verify(exactly = 1) { cardFieldsUseCase.updateField(PrimerInputElementType.CVV, cvv) }
    }

    @Test
    fun `updateExpiryDate should update expiry date field`() = runTest {
        val expiryDate = "12/25"
        viewModel.updateExpiryDate(expiryDate)

        verify(exactly = 1) { cardFieldsUseCase.updateField(PrimerInputElementType.EXPIRY_DATE, expiryDate) }
    }

    @Test
    fun `updateCardholderName should update cardholder name field`() = runTest {
        val name = "John Doe"
        viewModel.updateCardholderName(name)

        verify(exactly = 1) { cardFieldsUseCase.updateField(PrimerInputElementType.CARDHOLDER_NAME, name) }
    }

    @Test
    fun `updatePostalCode should update postal code field`() = runTest {
        val postalCode = "12345"
        viewModel.updatePostalCode(postalCode)

        verify(exactly = 1) { cardFieldsUseCase.updateField(PrimerInputElementType.POSTAL_CODE, postalCode) }
    }

    @Test
    fun `onSubmit when validation fails should not proceed`() = runTest {
        coEvery { cardFieldsUseCase.markSubmitAttempted() } returns Unit
        coEvery { cardFieldsUseCase.isSubmitAllowed() } returns false

        viewModel.submit()
        advanceUntilIdle()

        coVerify(exactly = 1) { cardFieldsUseCase.markSubmitAttempted() }
        coVerify(atLeast = 1) { cardFieldsUseCase.isSubmitAllowed() }
        coVerify(exactly = 0) { submitCardPaymentUseCase(any()) }
        verify(
            exactly = 1,
        ) { logReporter.debug("Validation failed, not proceeding with submission", component = any()) }
    }

    @Test
    fun `onSubmit when validation passes should submit payment and emit Success`() = runTest {
        val formData = mapOf(
            PrimerInputElementType.CARD_NUMBER to "4111111111111111",
            PrimerInputElementType.CVV to "123",
        )
        val payment = mockk<Payment>()
        every { payment.id } returns "payment-id"
        val checkoutData = mockk<PrimerCheckoutData>()
        every { checkoutData.payment } returns payment

        coEvery { cardFieldsUseCase.markSubmitAttempted() } returns Unit
        coEvery { cardFieldsUseCase.isSubmitAllowed() } returns true
        every { cardFieldsUseCase.formData } returns flowOf(formData)
        coEvery { submitCardPaymentUseCase(formData) } returns Result.success(checkoutData)

        viewModel.submit()
        advanceUntilIdle()

        coVerify(exactly = 1) { submitCardPaymentUseCase(formData) }
        verify(exactly = 1) { resultHandler.onSuccess(checkoutData) }
        verify(exactly = 1) { logReporter.info("Payment completed successfully", component = any()) }
    }

    @Test
    fun `onSubmit when payment fails should emit Error navigation event`() = runTest {
        val formData = mapOf(
            PrimerInputElementType.CARD_NUMBER to "4111111111111111",
            PrimerInputElementType.CVV to "123",
        )
        val errorMessage = "Payment declined"

        coEvery { cardFieldsUseCase.markSubmitAttempted() } returns Unit
        coEvery { cardFieldsUseCase.isSubmitAllowed() } returns true
        every { cardFieldsUseCase.formData } returns flowOf(formData)
        coEvery { submitCardPaymentUseCase(formData) } returns Result.failure(Exception(errorMessage))

        viewModel.submit()
        advanceUntilIdle()

        coVerify(exactly = 1) { submitCardPaymentUseCase(formData) }
        verify(exactly = 1) { resultHandler.onError(any()) }
        verify(exactly = 1) { logReporter.error("Payment failed: $errorMessage", component = any(), throwable = any()) }
    }

    @Test
    fun `onCountryClicked should call navigator navigateToCountrySelection`() = runTest {
        viewModel.requestCountrySelection()
        advanceUntilIdle()

        verify(exactly = 1) { countryNavigator.navigateToCountrySelection() }
    }

    @Test
    fun `selectCardNetwork should call use case`() = runTest {
        val network = PrimerCardNetwork(CardNetwork.Type.VISA, "Visa", allowed = true)
        viewModel.selectCardNetwork(network)

        verify(exactly = 1) { cardNetworkUseCase.selectCardNetwork(network) }
    }

    @Test
    fun `form data changes should update state`() = runTest {
        val formData = mapOf(
            PrimerInputElementType.CARD_NUMBER to "4111111111111111",
            PrimerInputElementType.CVV to "123",
        )

        formDataFlow.value = formData
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(formData, state.data)
    }

    @Test
    fun `validation errors should update state`() = runTest {
        val errors = listOf(
            SyncValidationError(
                inputElementType = PrimerInputElementType.CARD_NUMBER,
                errorId = "invalid_card_number",
                fieldId = 1,
            ),
        )

        validationErrorsFlow.value = errors
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(errors, state.fieldErrors)
    }

    @Test
    fun `network selection changes should update state when selectable`() = runTest {
        val visa = PrimerCardNetwork(CardNetwork.Type.VISA, "Visa", allowed = true)
        val networkSelection = PrimerCardFormController.NetworkSelection(
            selectedNetwork = visa,
            availableNetworks = listOf(visa),
            isNetworkSelectable = true,
        )
        networkSelectionFlow.value = networkSelection
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(networkSelection, state.networkSelection)
        verify(exactly = 1) { cardFieldsUseCase.updateCardNetwork(visa) }
    }

    @Test
    fun `network selection should send null to cardFieldsUseCase when not selectable`() = runTest {
        val visa = PrimerCardNetwork(CardNetwork.Type.VISA, "Visa", allowed = true)
        val networkSelection = PrimerCardFormController.NetworkSelection(
            selectedNetwork = visa,
            availableNetworks = listOf(visa),
            isNetworkSelectable = false,
        )
        networkSelectionFlow.value = networkSelection
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(networkSelection, state.networkSelection)
        verify { cardFieldsUseCase.updateCardNetwork(null) }
        verify(exactly = 0) { cardFieldsUseCase.updateCardNetwork(visa) }
    }

    @Test
    fun `country selection result from navigator should update selected country and field`() = runTest {
        val countryCode = "US"
        val countryName = "United States"
        val countryResultFlow = MutableSharedFlow<CountryNavigator.CountrySelectionResult>()
        every { countryNavigator.countrySelectionResult } returns countryResultFlow

        // Recreate viewModel to use the new mock
        viewModel = CardFormViewModel(
            cardFieldsUseCase = cardFieldsUseCase,
            cardNetworkUseCase = cardNetworkUseCase,
            submitCardPaymentUseCase = submitCardPaymentUseCase,
            setVaultOnSuccessUseCase = setVaultOnSuccessUseCase,
            cleanupUseCase = cleanupUseCase,
            logReporter = logReporter,
            componentsEventsRepository = componentsEventsRepository,
            countryNavigator = countryNavigator,
            resultHandler = resultHandler,
        )

        // Wait for collection to start
        advanceUntilIdle()

        // Emit country selection result
        countryResultFlow.emit(CountryNavigator.CountrySelectionResult(countryCode, countryName))
        advanceUntilIdle()

        verify(exactly = 1) { cardFieldsUseCase.updateField(PrimerInputElementType.COUNTRY_CODE, countryCode) }
        val state = viewModel.state.value
        assertEquals(countryName, state.selectedCountry?.name)
    }

    @Test
    fun `loading state should be set during payment submission`() = runTest {
        val formData = mapOf(
            PrimerInputElementType.CARD_NUMBER to "4111111111111111",
        )
        val payment = mockk<Payment>()
        every { payment.id } returns "payment-id"
        val checkoutData = mockk<PrimerCheckoutData>()
        every { checkoutData.payment } returns payment

        coEvery { cardFieldsUseCase.markSubmitAttempted() } returns Unit
        coEvery { cardFieldsUseCase.isSubmitAllowed() } returns true
        every { cardFieldsUseCase.formData } returns flowOf(formData)
        coEvery { submitCardPaymentUseCase(formData) } coAnswers {
            kotlinx.coroutines.delay(100)
            Result.success(checkoutData)
        }

        val stateChanges = mutableListOf<Boolean>()
        val job = launch {
            viewModel.state.collect { state ->
                stateChanges.add(state.isLoading)
            }
        }

        viewModel.submit()
        advanceUntilIdle()

        assertTrue(stateChanges.contains(true))
        assertFalse(viewModel.state.value.isLoading)

        job.cancel()
    }

    @Test
    fun `all update methods should delegate to use case`() = runTest {
        viewModel.updateCountryCode("US")
        viewModel.updateCity("New York")
        viewModel.updateState("NY")
        viewModel.updateAddressLine1("123 Main St")
        viewModel.updateAddressLine2("Apt 4")
        viewModel.updatePhoneNumber("+1234567890")
        viewModel.updateFirstName("John")
        viewModel.updateLastName("Doe")

        verify(exactly = 1) { cardFieldsUseCase.updateField(PrimerInputElementType.COUNTRY_CODE, "US") }
        verify(exactly = 1) { cardFieldsUseCase.updateField(PrimerInputElementType.CITY, "New York") }
        verify(exactly = 1) { cardFieldsUseCase.updateField(PrimerInputElementType.STATE, "NY") }
        verify(exactly = 1) { cardFieldsUseCase.updateField(PrimerInputElementType.ADDRESS_LINE_1, "123 Main St") }
        verify(exactly = 1) { cardFieldsUseCase.updateField(PrimerInputElementType.ADDRESS_LINE_2, "Apt 4") }
        verify(exactly = 1) { cardFieldsUseCase.updateField(PrimerInputElementType.PHONE_NUMBER, "+1234567890") }
        verify(exactly = 1) { cardFieldsUseCase.updateField(PrimerInputElementType.FIRST_NAME, "John") }
        verify(exactly = 1) { cardFieldsUseCase.updateField(PrimerInputElementType.LAST_NAME, "Doe") }
    }

    @Test
    fun `when all details entered should send PaymentDetailsEntered event`() = runTest {
        coEvery { cardFieldsUseCase.isSubmitAllowed() } returns true

        validationErrorsFlow.value = emptyList()
        advanceUntilIdle()

        verify(exactly = 1) {
            componentsEventsRepository.send(EventType.PaymentDetailsEntered.card(), any())
        }
    }

    @Test
    fun `PaymentDetailsEntered event should only fire once per session`() = runTest {
        coEvery { cardFieldsUseCase.isSubmitAllowed() } returns true

        validationErrorsFlow.value = emptyList()
        advanceUntilIdle()

        validationErrorsFlow.value = emptyList()
        advanceUntilIdle()

        verify(exactly = 1) {
            componentsEventsRepository.send(EventType.PaymentDetailsEntered.card(), any())
        }
    }

    @Test
    fun `onSubmit should send PaymentSubmitted event`() = runTest {
        coEvery { cardFieldsUseCase.markSubmitAttempted() } returns Unit
        coEvery { cardFieldsUseCase.isSubmitAllowed() } returns false

        viewModel.submit()
        advanceUntilIdle()

        verify(exactly = 1) {
            componentsEventsRepository.send(EventType.PaymentSubmitted.card(), any())
        }
    }

    @Test
    fun `onSubmit when validation passes should send PaymentProcessingStarted event`() = runTest {
        val formData = mapOf(
            PrimerInputElementType.CARD_NUMBER to "4111111111111111",
        )
        val payment = mockk<Payment>()
        every { payment.id } returns "payment-id"
        val checkoutData = mockk<PrimerCheckoutData>()
        every { checkoutData.payment } returns payment

        coEvery { cardFieldsUseCase.markSubmitAttempted() } returns Unit
        coEvery { cardFieldsUseCase.isSubmitAllowed() } returns true
        every { cardFieldsUseCase.formData } returns flowOf(formData)
        coEvery { submitCardPaymentUseCase(formData) } returns Result.success(checkoutData)

        viewModel.submit()
        advanceUntilIdle()

        verify(exactly = 1) {
            componentsEventsRepository.send(EventType.PaymentProcessingStarted.card(), any())
        }
    }

    @Test
    fun `onSubmit when payment succeeds should send PaymentSuccess event with paymentId`() = runTest {
        val formData = mapOf(
            PrimerInputElementType.CARD_NUMBER to "4111111111111111",
        )
        val paymentId = "payment-123-abc"
        val payment = mockk<Payment>()
        every { payment.id } returns paymentId

        val checkoutData = mockk<PrimerCheckoutData>()
        every { checkoutData.payment } returns payment

        coEvery { cardFieldsUseCase.markSubmitAttempted() } returns Unit
        coEvery { cardFieldsUseCase.isSubmitAllowed() } returns true
        every { cardFieldsUseCase.formData } returns flowOf(formData)
        coEvery { submitCardPaymentUseCase(formData) } returns Result.success(checkoutData)

        viewModel.submit()
        advanceUntilIdle()

        verify(exactly = 1) {
            componentsEventsRepository.send(EventType.PaymentSuccess.card(paymentId), any())
        }
    }

    @Test
    fun `onSubmit when payment fails should send PaymentFailure event`() = runTest {
        val formData = mapOf(
            PrimerInputElementType.CARD_NUMBER to "4111111111111111",
        )

        coEvery { cardFieldsUseCase.markSubmitAttempted() } returns Unit
        coEvery { cardFieldsUseCase.isSubmitAllowed() } returns true
        every { cardFieldsUseCase.formData } returns flowOf(formData)
        coEvery { submitCardPaymentUseCase(formData) } returns Result.failure(Exception("Payment declined"))

        viewModel.submit()
        advanceUntilIdle()

        verify(exactly = 1) {
            componentsEventsRepository.send(EventType.PaymentFailure.card(), any())
        }
    }

    @Test
    fun `onSubmit should send events in correct order on success`() = runTest {
        val formData = mapOf(
            PrimerInputElementType.CARD_NUMBER to "4111111111111111",
        )
        val paymentId = "payment-456"
        val payment = mockk<Payment>()
        every { payment.id } returns paymentId

        val checkoutData = mockk<PrimerCheckoutData>()
        every { checkoutData.payment } returns payment

        coEvery { cardFieldsUseCase.markSubmitAttempted() } returns Unit
        coEvery { cardFieldsUseCase.isSubmitAllowed() } returns true
        every { cardFieldsUseCase.formData } returns flowOf(formData)
        coEvery { submitCardPaymentUseCase(formData) } returns Result.success(checkoutData)

        viewModel.submit()
        advanceUntilIdle()

        verifyOrder {
            componentsEventsRepository.send(EventType.PaymentSubmitted.card(), any())
            componentsEventsRepository.send(EventType.PaymentProcessingStarted.card(), any())
            componentsEventsRepository.send(EventType.PaymentSuccess.card(paymentId), any())
        }
    }

    @Test
    fun `onSubmit should send events in correct order on failure`() = runTest {
        val formData = mapOf(
            PrimerInputElementType.CARD_NUMBER to "4111111111111111",
        )

        coEvery { cardFieldsUseCase.markSubmitAttempted() } returns Unit
        coEvery { cardFieldsUseCase.isSubmitAllowed() } returns true
        every { cardFieldsUseCase.formData } returns flowOf(formData)
        coEvery { submitCardPaymentUseCase(formData) } returns Result.failure(Exception("Declined"))

        viewModel.submit()
        advanceUntilIdle()

        verifyOrder {
            componentsEventsRepository.send(EventType.PaymentSubmitted.card(), any())
            componentsEventsRepository.send(EventType.PaymentProcessingStarted.card(), any())
            componentsEventsRepository.send(EventType.PaymentFailure.card(), any())
        }
    }

    @Test
    fun `vaultOnSuccess with true should update state on success`() = runTest {
        coEvery { setVaultOnSuccessUseCase(true) } returns Result.success(mockk())

        viewModel.setVaultOnSuccess(true)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.vaultOnSuccess)
        assertFalse(state.isLoading)
        coVerify(exactly = 1) { setVaultOnSuccessUseCase(true) }
    }

    @Test
    fun `vaultOnSuccess with false should update state on success`() = runTest {
        coEvery { setVaultOnSuccessUseCase(false) } returns Result.success(mockk())

        viewModel.setVaultOnSuccess(false)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.vaultOnSuccess)
        assertFalse(state.isLoading)
        coVerify(exactly = 1) { setVaultOnSuccessUseCase(false) }
    }

    @Test
    fun `vaultOnSuccess should set loading state during operation`() = runTest {
        coEvery { setVaultOnSuccessUseCase(true) } coAnswers {
            kotlinx.coroutines.delay(100)
            Result.success(mockk())
        }

        val loadingStates = mutableListOf<Boolean>()
        val job = launch {
            viewModel.state.collect { state ->
                loadingStates.add(state.isLoading)
            }
        }

        viewModel.setVaultOnSuccess(true)
        advanceUntilIdle()

        assertTrue(loadingStates.contains(true))
        assertFalse(viewModel.state.value.isLoading)

        job.cancel()
    }

    @Test
    fun `vaultOnSuccess with true should revert to false on failure`() = runTest {
        val error = Exception("Network error")
        coEvery { setVaultOnSuccessUseCase(true) } returns Result.failure(error)

        viewModel.setVaultOnSuccess(true)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.vaultOnSuccess)
        assertFalse(state.isLoading)
        verify(exactly = 1) { logReporter.warn("Failed to set vaultOnSuccess=true: Network error", component = any()) }
    }

    @Test
    fun `vaultOnSuccess with false should revert to true on failure`() = runTest {
        val error = Exception("Network error")
        coEvery { setVaultOnSuccessUseCase(false) } returns Result.failure(error)

        viewModel.setVaultOnSuccess(false)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.vaultOnSuccess)
        assertFalse(state.isLoading)
        verify(exactly = 1) { logReporter.warn("Failed to set vaultOnSuccess=false: Network error", component = any()) }
    }
}
