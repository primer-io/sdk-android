package io.primer.android.internal.presentation.screens.card

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.configuration.data.model.CountryCode
import io.primer.android.core.InstantExecutorExtension
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.internal.domain.usecase.CardFieldsUseCase
import io.primer.android.internal.domain.usecase.CardNetworkUseCase
import io.primer.android.internal.domain.usecase.SubmitCardPaymentUseCase
import io.primer.android.internal.presentation.checkout.CheckoutNavigator
import io.primer.android.internal.presentation.checkout.Screen
import io.primer.android.ui.core.model.SyncValidationError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
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
    private lateinit var checkoutNavigator: CheckoutNavigator
    private lateinit var logReporter: LogReporter
    private lateinit var viewModel: CardFormViewModel

    private val formDataFlow = MutableStateFlow(emptyMap<PrimerInputElementType, String>())
    private val validationErrorsFlow = MutableStateFlow<List<SyncValidationError>>(emptyList())
    private val currentCardNetworkFlow = MutableStateFlow(CardNetwork.Type.OTHER)
    private val availableNetworksFlow = MutableStateFlow(
        emptyList<io.primer.android.components.domain.core.models.card.PrimerCardNetwork>(),
    )

    @BeforeEach
    fun setup() {
        cardFieldsUseCase = mockk(relaxed = true)
        cardNetworkUseCase = mockk(relaxed = true)
        submitCardPaymentUseCase = mockk()
        checkoutNavigator = mockk(relaxed = true)
        logReporter = mockk(relaxed = true)

        every { cardFieldsUseCase.formData } returns formDataFlow
        every { cardFieldsUseCase.validationErrors } returns validationErrorsFlow
        every { cardNetworkUseCase.currentCardNetwork } returns currentCardNetworkFlow
        every { cardNetworkUseCase.availableNetworks } returns availableNetworksFlow
        every {
            checkoutNavigator.observeNavigationResult<Pair<String, String>>("selected_country")
        } returns flowOf(null)
    }

    @Test
    fun `constructor should store dependencies correctly`() = runTest {
        coEvery { cardFieldsUseCase.getCardFields() } returns listOf(
            PrimerInputElementType.CARD_NUMBER,
            PrimerInputElementType.EXPIRY_DATE,
            PrimerInputElementType.CVV,
        )
        coEvery { cardFieldsUseCase.getBillingFields() } returns emptyList()

        viewModel = CardFormViewModel(
            cardFieldsUseCase = cardFieldsUseCase,
            cardNetworkUseCase = cardNetworkUseCase,
            submitCardPaymentUseCase = submitCardPaymentUseCase,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )

        assertNotNull(viewModel)
    }

    @Test
    fun `should initialize state with card and billing fields`() = runTest {
        val cardFields = listOf(
            PrimerInputElementType.CARD_NUMBER,
            PrimerInputElementType.EXPIRY_DATE,
            PrimerInputElementType.CVV,
        )
        val billingFields = listOf(
            PrimerInputElementType.POSTAL_CODE,
            PrimerInputElementType.COUNTRY_CODE,
        )

        coEvery { cardFieldsUseCase.getCardFields() } returns cardFields
        coEvery { cardFieldsUseCase.getBillingFields() } returns billingFields

        viewModel = CardFormViewModel(
            cardFieldsUseCase = cardFieldsUseCase,
            cardNetworkUseCase = cardNetworkUseCase,
            submitCardPaymentUseCase = submitCardPaymentUseCase,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )

        advanceUntilIdle()

        val state = viewModel.state.first()
        assertEquals(cardFields, state.cardFields)
        assertEquals(billingFields, state.billingFields)
    }

    @Test
    fun `updateCardNumber should update field and detect network`() = runTest {
        coEvery { cardFieldsUseCase.getCardFields() } returns emptyList()
        coEvery { cardFieldsUseCase.getBillingFields() } returns emptyList()

        viewModel = CardFormViewModel(
            cardFieldsUseCase = cardFieldsUseCase,
            cardNetworkUseCase = cardNetworkUseCase,
            submitCardPaymentUseCase = submitCardPaymentUseCase,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )

        val cardNumber = "4111111111111111"
        viewModel.updateCardNumber(cardNumber)

        verify(exactly = 1) { cardFieldsUseCase.updateField(PrimerInputElementType.CARD_NUMBER, cardNumber) }
        verify(exactly = 1) { cardNetworkUseCase.detectCardNetwork(cardNumber) }
    }

    @Test
    fun `updateCardNumber with empty string should clear network`() = runTest {
        coEvery { cardFieldsUseCase.getCardFields() } returns emptyList()
        coEvery { cardFieldsUseCase.getBillingFields() } returns emptyList()

        viewModel = CardFormViewModel(
            cardFieldsUseCase = cardFieldsUseCase,
            cardNetworkUseCase = cardNetworkUseCase,
            submitCardPaymentUseCase = submitCardPaymentUseCase,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )

        viewModel.updateCardNumber("")

        verify(exactly = 1) { cardNetworkUseCase.clear() }
        verify(exactly = 1) { cardFieldsUseCase.updateField(PrimerInputElementType.CARD_NUMBER, "") }
    }

    @Test
    fun `updateCvv should update CVV field`() = runTest {
        coEvery { cardFieldsUseCase.getCardFields() } returns emptyList()
        coEvery { cardFieldsUseCase.getBillingFields() } returns emptyList()

        viewModel = CardFormViewModel(
            cardFieldsUseCase = cardFieldsUseCase,
            cardNetworkUseCase = cardNetworkUseCase,
            submitCardPaymentUseCase = submitCardPaymentUseCase,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )

        val cvv = "123"
        viewModel.updateCvv(cvv)

        verify(exactly = 1) { cardFieldsUseCase.updateField(PrimerInputElementType.CVV, cvv) }
    }

    @Test
    fun `updateExpiryDate should update expiry date field`() = runTest {
        coEvery { cardFieldsUseCase.getCardFields() } returns emptyList()
        coEvery { cardFieldsUseCase.getBillingFields() } returns emptyList()

        viewModel = CardFormViewModel(
            cardFieldsUseCase = cardFieldsUseCase,
            cardNetworkUseCase = cardNetworkUseCase,
            submitCardPaymentUseCase = submitCardPaymentUseCase,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )

        val expiryDate = "12/25"
        viewModel.updateExpiryDate(expiryDate)

        verify(exactly = 1) { cardFieldsUseCase.updateField(PrimerInputElementType.EXPIRY_DATE, expiryDate) }
    }

    @Test
    fun `updateCardholderName should update cardholder name field`() = runTest {
        coEvery { cardFieldsUseCase.getCardFields() } returns emptyList()
        coEvery { cardFieldsUseCase.getBillingFields() } returns emptyList()

        viewModel = CardFormViewModel(
            cardFieldsUseCase = cardFieldsUseCase,
            cardNetworkUseCase = cardNetworkUseCase,
            submitCardPaymentUseCase = submitCardPaymentUseCase,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )

        val name = "John Doe"
        viewModel.updateCardholderName(name)

        verify(exactly = 1) { cardFieldsUseCase.updateField(PrimerInputElementType.CARDHOLDER_NAME, name) }
    }

    @Test
    fun `updatePostalCode should update postal code field`() = runTest {
        coEvery { cardFieldsUseCase.getCardFields() } returns emptyList()
        coEvery { cardFieldsUseCase.getBillingFields() } returns emptyList()

        viewModel = CardFormViewModel(
            cardFieldsUseCase = cardFieldsUseCase,
            cardNetworkUseCase = cardNetworkUseCase,
            submitCardPaymentUseCase = submitCardPaymentUseCase,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )

        val postalCode = "12345"
        viewModel.updatePostalCode(postalCode)

        verify(exactly = 1) { cardFieldsUseCase.updateField(PrimerInputElementType.POSTAL_CODE, postalCode) }
    }

    @Test
    fun `onSubmit when validation fails should not proceed`() = runTest {
        coEvery { cardFieldsUseCase.getCardFields() } returns emptyList()
        coEvery { cardFieldsUseCase.getBillingFields() } returns emptyList()
        coEvery { cardFieldsUseCase.markSubmitAttempted() } returns Unit
        coEvery { cardFieldsUseCase.isSubmitAllowed() } returns false

        viewModel = CardFormViewModel(
            cardFieldsUseCase = cardFieldsUseCase,
            cardNetworkUseCase = cardNetworkUseCase,
            submitCardPaymentUseCase = submitCardPaymentUseCase,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )

        viewModel.onSubmit()
        advanceUntilIdle()

        coVerify(exactly = 1) { cardFieldsUseCase.markSubmitAttempted() }
        coVerify(exactly = 1) { cardFieldsUseCase.isSubmitAllowed() }
        coVerify(exactly = 0) { submitCardPaymentUseCase(any()) }
        verify(exactly = 1) { logReporter.debug("Validation failed, not proceeding with submission") }
    }

    @Test
    fun `onSubmit when validation passes should submit payment`() = runTest {
        val formData = mapOf(
            PrimerInputElementType.CARD_NUMBER to "4111111111111111",
            PrimerInputElementType.CVV to "123",
        )

        coEvery { cardFieldsUseCase.getCardFields() } returns emptyList()
        coEvery { cardFieldsUseCase.getBillingFields() } returns emptyList()
        coEvery { cardFieldsUseCase.markSubmitAttempted() } returns Unit
        coEvery { cardFieldsUseCase.isSubmitAllowed() } returns true
        every { cardFieldsUseCase.formData } returns flowOf(formData)
        coEvery { submitCardPaymentUseCase(formData) } returns Result.success(mockk<PrimerCheckoutData>())

        viewModel = CardFormViewModel(
            cardFieldsUseCase = cardFieldsUseCase,
            cardNetworkUseCase = cardNetworkUseCase,
            submitCardPaymentUseCase = submitCardPaymentUseCase,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )

        viewModel.onSubmit()
        advanceUntilIdle()

        coVerify(exactly = 1) { submitCardPaymentUseCase(formData) }
        coVerify(exactly = 1) { checkoutNavigator.navigateToSuccess() }
        verify(exactly = 1) { logReporter.debug("Payment completed successfully") }
    }

    @Test
    fun `onSubmit when payment fails should navigate to error`() = runTest {
        val formData = mapOf(
            PrimerInputElementType.CARD_NUMBER to "4111111111111111",
            PrimerInputElementType.CVV to "123",
        )
        val errorMessage = "Payment declined"

        coEvery { cardFieldsUseCase.getCardFields() } returns emptyList()
        coEvery { cardFieldsUseCase.getBillingFields() } returns emptyList()
        coEvery { cardFieldsUseCase.markSubmitAttempted() } returns Unit
        coEvery { cardFieldsUseCase.isSubmitAllowed() } returns true
        every { cardFieldsUseCase.formData } returns flowOf(formData)
        coEvery { submitCardPaymentUseCase(formData) } returns Result.failure(Exception(errorMessage))

        viewModel = CardFormViewModel(
            cardFieldsUseCase = cardFieldsUseCase,
            cardNetworkUseCase = cardNetworkUseCase,
            submitCardPaymentUseCase = submitCardPaymentUseCase,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )

        viewModel.onSubmit()
        advanceUntilIdle()

        coVerify(exactly = 1) { submitCardPaymentUseCase(formData) }
        coVerify(exactly = 1) { checkoutNavigator.navigateToError(errorMessage) }
        verify(exactly = 1) { logReporter.error("Payment failed: $errorMessage") }
    }

    @Test
    fun `onBack should navigate back`() = runTest {
        coEvery { cardFieldsUseCase.getCardFields() } returns emptyList()
        coEvery { cardFieldsUseCase.getBillingFields() } returns emptyList()

        viewModel = CardFormViewModel(
            cardFieldsUseCase = cardFieldsUseCase,
            cardNetworkUseCase = cardNetworkUseCase,
            submitCardPaymentUseCase = submitCardPaymentUseCase,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )

        viewModel.onBack()
        advanceUntilIdle()

        coVerify(exactly = 1) { checkoutNavigator.navigateBack() }
    }

    @Test
    fun `onCancel should dismiss`() = runTest {
        coEvery { cardFieldsUseCase.getCardFields() } returns emptyList()
        coEvery { cardFieldsUseCase.getBillingFields() } returns emptyList()

        viewModel = CardFormViewModel(
            cardFieldsUseCase = cardFieldsUseCase,
            cardNetworkUseCase = cardNetworkUseCase,
            submitCardPaymentUseCase = submitCardPaymentUseCase,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )

        viewModel.onCancel()
        advanceUntilIdle()

        coVerify(exactly = 1) { checkoutNavigator.dismiss() }
    }

    @Test
    fun `navigateToCountrySelection should navigate to SelectCountry screen`() = runTest {
        coEvery { cardFieldsUseCase.getCardFields() } returns emptyList()
        coEvery { cardFieldsUseCase.getBillingFields() } returns emptyList()

        viewModel = CardFormViewModel(
            cardFieldsUseCase = cardFieldsUseCase,
            cardNetworkUseCase = cardNetworkUseCase,
            submitCardPaymentUseCase = submitCardPaymentUseCase,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )

        viewModel.navigateToCountrySelection()
        advanceUntilIdle()

        coVerify(exactly = 1) { checkoutNavigator.navigateTo(Screen.SelectCountry) }
    }

    @Test
    fun `selectCardNetwork should call use case`() = runTest {
        coEvery { cardFieldsUseCase.getCardFields() } returns emptyList()
        coEvery { cardFieldsUseCase.getBillingFields() } returns emptyList()

        viewModel = CardFormViewModel(
            cardFieldsUseCase = cardFieldsUseCase,
            cardNetworkUseCase = cardNetworkUseCase,
            submitCardPaymentUseCase = submitCardPaymentUseCase,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )

        val network = CardNetwork.Type.VISA
        viewModel.selectCardNetwork(network)

        verify(exactly = 1) { cardNetworkUseCase.selectCardNetwork(network) }
    }

    @Test
    fun `form data changes should update state`() = runTest {
        val formData = mapOf(
            PrimerInputElementType.CARD_NUMBER to "4111111111111111",
            PrimerInputElementType.CVV to "123",
        )

        coEvery { cardFieldsUseCase.getCardFields() } returns emptyList()
        coEvery { cardFieldsUseCase.getBillingFields() } returns emptyList()

        viewModel = CardFormViewModel(
            cardFieldsUseCase = cardFieldsUseCase,
            cardNetworkUseCase = cardNetworkUseCase,
            submitCardPaymentUseCase = submitCardPaymentUseCase,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
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

        coEvery { cardFieldsUseCase.getCardFields() } returns emptyList()
        coEvery { cardFieldsUseCase.getBillingFields() } returns emptyList()

        viewModel = CardFormViewModel(
            cardFieldsUseCase = cardFieldsUseCase,
            cardNetworkUseCase = cardNetworkUseCase,
            submitCardPaymentUseCase = submitCardPaymentUseCase,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )

        validationErrorsFlow.value = errors
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(errors, state.fieldErrors)
    }

    @Test
    fun `card network changes should update state`() = runTest {
        coEvery { cardFieldsUseCase.getCardFields() } returns emptyList()
        coEvery { cardFieldsUseCase.getBillingFields() } returns emptyList()

        viewModel = CardFormViewModel(
            cardFieldsUseCase = cardFieldsUseCase,
            cardNetworkUseCase = cardNetworkUseCase,
            submitCardPaymentUseCase = submitCardPaymentUseCase,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )

        currentCardNetworkFlow.value = CardNetwork.Type.VISA
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(CardNetwork.Type.VISA, state.selectedNetwork)
        verify(exactly = 1) { cardFieldsUseCase.updateCardNetwork(CardNetwork.Type.VISA) }
    }

    @Test
    fun `country selection should update state and field`() = runTest {
        val countryCode = "US"
        val countryName = "United States"
        val countrySelectionFlow = MutableStateFlow<Pair<String, String>?>(null)

        coEvery { cardFieldsUseCase.getCardFields() } returns emptyList()
        coEvery { cardFieldsUseCase.getBillingFields() } returns emptyList()
        every {
            checkoutNavigator.observeNavigationResult<Pair<String, String>>("selected_country")
        } returns countrySelectionFlow

        viewModel = CardFormViewModel(
            cardFieldsUseCase = cardFieldsUseCase,
            cardNetworkUseCase = cardNetworkUseCase,
            submitCardPaymentUseCase = submitCardPaymentUseCase,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )

        countrySelectionFlow.value = Pair(countryCode, countryName)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNotNull(state.selectedCountry)
        assertEquals(countryName, state.selectedCountry?.name)
        assertEquals(CountryCode.US, state.selectedCountry?.code)
        verify(exactly = 1) { cardFieldsUseCase.updateField(PrimerInputElementType.COUNTRY_CODE, countryCode) }
    }

    @Test
    fun `loading state should be set during payment submission`() = runTest {
        val formData = mapOf(
            PrimerInputElementType.CARD_NUMBER to "4111111111111111",
        )

        coEvery { cardFieldsUseCase.getCardFields() } returns emptyList()
        coEvery { cardFieldsUseCase.getBillingFields() } returns emptyList()
        coEvery { cardFieldsUseCase.markSubmitAttempted() } returns Unit
        coEvery { cardFieldsUseCase.isSubmitAllowed() } returns true
        every { cardFieldsUseCase.formData } returns flowOf(formData)
        coEvery { submitCardPaymentUseCase(formData) } coAnswers {
            kotlinx.coroutines.delay(100)
            Result.success(mockk<PrimerCheckoutData>())
        }

        viewModel = CardFormViewModel(
            cardFieldsUseCase = cardFieldsUseCase,
            cardNetworkUseCase = cardNetworkUseCase,
            submitCardPaymentUseCase = submitCardPaymentUseCase,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )

        val stateChanges = mutableListOf<Boolean>()
        val job = launch {
            viewModel.state.collect { state ->
                stateChanges.add(state.isLoading)
            }
        }

        viewModel.onSubmit()
        advanceUntilIdle()

        assertTrue(stateChanges.contains(true))
        assertFalse(viewModel.state.value.isLoading)

        job.cancel()
    }

    @Test
    fun `all update methods should delegate to use case`() = runTest {
        coEvery { cardFieldsUseCase.getCardFields() } returns emptyList()
        coEvery { cardFieldsUseCase.getBillingFields() } returns emptyList()

        viewModel = CardFormViewModel(
            cardFieldsUseCase = cardFieldsUseCase,
            cardNetworkUseCase = cardNetworkUseCase,
            submitCardPaymentUseCase = submitCardPaymentUseCase,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )

        viewModel.updateCountryCode("US")
        viewModel.updateCity("New York")
        viewModel.updateState("NY")
        viewModel.updateAddressLine1("123 Main St")
        viewModel.updateAddressLine2("Apt 4")
        viewModel.updatePhoneNumber("+1234567890")
        viewModel.updateFirstName("John")
        viewModel.updateLastName("Doe")
        viewModel.updateRetailOutlet("Store 1")
        viewModel.updateOtpCode("123456")

        verify(exactly = 1) { cardFieldsUseCase.updateField(PrimerInputElementType.COUNTRY_CODE, "US") }
        verify(exactly = 1) { cardFieldsUseCase.updateField(PrimerInputElementType.CITY, "New York") }
        verify(exactly = 1) { cardFieldsUseCase.updateField(PrimerInputElementType.STATE, "NY") }
        verify(exactly = 1) { cardFieldsUseCase.updateField(PrimerInputElementType.ADDRESS_LINE_1, "123 Main St") }
        verify(exactly = 1) { cardFieldsUseCase.updateField(PrimerInputElementType.ADDRESS_LINE_2, "Apt 4") }
        verify(exactly = 1) { cardFieldsUseCase.updateField(PrimerInputElementType.PHONE_NUMBER, "+1234567890") }
        verify(exactly = 1) { cardFieldsUseCase.updateField(PrimerInputElementType.FIRST_NAME, "John") }
        verify(exactly = 1) { cardFieldsUseCase.updateField(PrimerInputElementType.LAST_NAME, "Doe") }
        verify(exactly = 1) { cardFieldsUseCase.updateField(PrimerInputElementType.RETAIL_OUTLET, "Store 1") }
        verify(exactly = 1) { cardFieldsUseCase.updateField(PrimerInputElementType.OTP_CODE, "123456") }
    }
}
