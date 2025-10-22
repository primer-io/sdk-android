package io.primer.android.internal.presentation.screens.paymentMethodSelection

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.analytics.data.model.EventType
import io.primer.android.components.analytics.data.repository.ComponentsEventsRepository
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.configuration.domain.model.Surcharge
import io.primer.android.core.InstantExecutorExtension
import io.primer.android.core.domain.None
import io.primer.android.internal.domain.models.PrimerComposablePaymentMethod
import io.primer.android.internal.domain.usecase.AvailablePaymentMethodsUseCase
import io.primer.android.internal.presentation.checkout.CheckoutNavigator
import io.primer.android.internal.presentation.checkout.Screen
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.ui.core.configuration.domain.model.BasicOrderInfo
import io.primer.android.ui.core.configuration.domain.model.BasicOrderInfoInteractor
import io.primer.android.ui.core.domain.FormatAmountToCurrencyInteractor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantExecutorExtension::class)
class PaymentMethodSelectionViewModelTest {

    private lateinit var basicOrderInfoInteractor: BasicOrderInfoInteractor
    private lateinit var checkoutNavigator: CheckoutNavigator
    private lateinit var availablePaymentMethodsUseCase: AvailablePaymentMethodsUseCase
    private lateinit var formatAmountToCurrencyInteractor: FormatAmountToCurrencyInteractor
    private lateinit var componentsEventsRepository: ComponentsEventsRepository
    private lateinit var viewModel: PaymentMethodSelectionViewModel

    @BeforeEach
    fun setup() {
        basicOrderInfoInteractor = mockk()
        checkoutNavigator = mockk(relaxed = true)
        availablePaymentMethodsUseCase = mockk()
        formatAmountToCurrencyInteractor = mockk()
        componentsEventsRepository = mockk(relaxed = true)
    }

    private fun createViewModel(paymentMethods: List<PrimerComposablePaymentMethod> = emptyList()): PaymentMethodSelectionViewModel {
        every { availablePaymentMethodsUseCase.cache } returns paymentMethods
        return PaymentMethodSelectionViewModel(
            basicOrderInfoInteractor = basicOrderInfoInteractor,
            checkoutNavigator = checkoutNavigator,
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            formatAmountToCurrencyInteractor = formatAmountToCurrencyInteractor,
            componentsEventsRepository = componentsEventsRepository,
        )
    }

    @Test
    fun `constructor should store dependencies correctly`() {
        every { availablePaymentMethodsUseCase.cache } returns emptyList()

        viewModel = PaymentMethodSelectionViewModel(
            basicOrderInfoInteractor = basicOrderInfoInteractor,
            checkoutNavigator = checkoutNavigator,
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            formatAmountToCurrencyInteractor = formatAmountToCurrencyInteractor,
            componentsEventsRepository = componentsEventsRepository,
        )

        assertNotNull(viewModel)
    }

    @Test
    fun `should initialize state with payment methods from cache`() = runTest {
        val paymentMethods = listOf(
            PrimerComposablePaymentMethod(
                paymentMethodType = "PAYMENT_CARD",
                paymentMethodName = "Card",
                supportedPrimerSessionIntents = listOf(PrimerSessionIntent.CHECKOUT),
                paymentMethodManagerCategories = listOf(PrimerPaymentMethodManagerCategory.NATIVE_UI),
            ),
        )

        every { availablePaymentMethodsUseCase.cache } returns paymentMethods

        viewModel = PaymentMethodSelectionViewModel(
            basicOrderInfoInteractor = basicOrderInfoInteractor,
            checkoutNavigator = checkoutNavigator,
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            formatAmountToCurrencyInteractor = formatAmountToCurrencyInteractor,
            componentsEventsRepository = componentsEventsRepository,
        )

        val state = viewModel.state.first()
        assertEquals(paymentMethods, state.paymentMethods)
    }

    @Test
    fun `init should load payment methods from cache`() = runTest {
        val paymentMethods = listOf(
            PrimerComposablePaymentMethod(
                paymentMethodType = "PAYPAL",
                paymentMethodName = "PayPal",
                supportedPrimerSessionIntents = listOf(PrimerSessionIntent.CHECKOUT),
                paymentMethodManagerCategories = listOf(
                    PrimerPaymentMethodManagerCategory.COMPONENT_WITH_REDIRECT,
                ),
            ),
            PrimerComposablePaymentMethod(
                paymentMethodType = "PAYMENT_CARD",
                paymentMethodName = "Card",
                supportedPrimerSessionIntents = listOf(PrimerSessionIntent.CHECKOUT),
                paymentMethodManagerCategories = listOf(PrimerPaymentMethodManagerCategory.NATIVE_UI),
            ),
        )

        every { availablePaymentMethodsUseCase.cache } returns paymentMethods

        viewModel = PaymentMethodSelectionViewModel(
            basicOrderInfoInteractor = basicOrderInfoInteractor,
            checkoutNavigator = checkoutNavigator,
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            formatAmountToCurrencyInteractor = formatAmountToCurrencyInteractor,
            componentsEventsRepository = componentsEventsRepository,
        )

        verify(exactly = 1) { availablePaymentMethodsUseCase.cache }

        val state = viewModel.state.value
        assertEquals(paymentMethods, state.paymentMethods)
    }

    @Test
    fun `formatTitleAmount should format amount using basicOrderInfoInteractor`() = runTest {
        val orderInfo = BasicOrderInfo(totalAmount = 1000, currencyCode = "USD")
        val expectedFormat = "$10.00"

        every { basicOrderInfoInteractor(None) } returns orderInfo
        every { availablePaymentMethodsUseCase.cache } returns emptyList()
        every {
            formatAmountToCurrencyInteractor.execute(any())
        } returns expectedFormat

        viewModel = PaymentMethodSelectionViewModel(
            basicOrderInfoInteractor = basicOrderInfoInteractor,
            checkoutNavigator = checkoutNavigator,
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            formatAmountToCurrencyInteractor = formatAmountToCurrencyInteractor,
            componentsEventsRepository = componentsEventsRepository,
        )

        val formatted = viewModel.formatTitleAmount()

        assertEquals(expectedFormat, formatted)
        verify(exactly = 1) {
            formatAmountToCurrencyInteractor.execute(any())
        }
    }

    @Test
    fun `formatSurchargeAmount should format with plus sign prefix`() = runTest {
        val orderInfo = BasicOrderInfo(totalAmount = 1000, currencyCode = "EUR")
        val surchargeAmount = 250
        val expectedFormat = "€2.50"

        every { basicOrderInfoInteractor(None) } returns orderInfo
        every { availablePaymentMethodsUseCase.cache } returns emptyList()
        every {
            formatAmountToCurrencyInteractor.execute(any())
        } returns expectedFormat

        viewModel = PaymentMethodSelectionViewModel(
            basicOrderInfoInteractor = basicOrderInfoInteractor,
            checkoutNavigator = checkoutNavigator,
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            formatAmountToCurrencyInteractor = formatAmountToCurrencyInteractor,
            componentsEventsRepository = componentsEventsRepository,
        )

        val formatted = viewModel.formatSurchargeAmount(surchargeAmount)

        assertEquals("+ €2.50", formatted)
        verify(exactly = 1) {
            formatAmountToCurrencyInteractor.execute(any())
        }
    }

    @Test
    fun `formatSurchargeAmount should return empty string when formatting fails`() = runTest {
        val orderInfo = BasicOrderInfo(totalAmount = 1000, currencyCode = "INVALID")

        every { basicOrderInfoInteractor(None) } returns orderInfo
        every { availablePaymentMethodsUseCase.cache } returns emptyList()
        every {
            formatAmountToCurrencyInteractor.execute(any())
        } returns ""

        viewModel = PaymentMethodSelectionViewModel(
            basicOrderInfoInteractor = basicOrderInfoInteractor,
            checkoutNavigator = checkoutNavigator,
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            formatAmountToCurrencyInteractor = formatAmountToCurrencyInteractor,
            componentsEventsRepository = componentsEventsRepository,
        )

        val formatted = viewModel.formatSurchargeAmount(100)

        assertEquals("", formatted)
    }

    @Test
    fun `onPaymentMethodSelected with PAYMENT_CARD should navigate to CardForm`() = runTest {
        every { availablePaymentMethodsUseCase.cache } returns emptyList()

        viewModel = PaymentMethodSelectionViewModel(
            basicOrderInfoInteractor = basicOrderInfoInteractor,
            checkoutNavigator = checkoutNavigator,
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            formatAmountToCurrencyInteractor = formatAmountToCurrencyInteractor,
            componentsEventsRepository = componentsEventsRepository,
        )

        viewModel.onPaymentMethodSelected(PaymentMethodType.PAYMENT_CARD.name)
        advanceUntilIdle()

        coVerify(exactly = 1) { checkoutNavigator.navigateTo(Screen.CardForm) }
    }

    @Test
    fun `onPaymentMethodSelected with unknown payment method should not navigate`() = runTest {
        every { availablePaymentMethodsUseCase.cache } returns emptyList()

        viewModel = PaymentMethodSelectionViewModel(
            basicOrderInfoInteractor = basicOrderInfoInteractor,
            checkoutNavigator = checkoutNavigator,
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            formatAmountToCurrencyInteractor = formatAmountToCurrencyInteractor,
            componentsEventsRepository = componentsEventsRepository,
        )

        viewModel.onPaymentMethodSelected("UNKNOWN_METHOD")
        advanceUntilIdle()

        coVerify(exactly = 0) { checkoutNavigator.navigateTo(any()) }
    }

    @Test
    fun `onCancel should call dismiss on navigator`() = runTest {
        every { availablePaymentMethodsUseCase.cache } returns emptyList()

        viewModel = PaymentMethodSelectionViewModel(
            basicOrderInfoInteractor = basicOrderInfoInteractor,
            checkoutNavigator = checkoutNavigator,
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            formatAmountToCurrencyInteractor = formatAmountToCurrencyInteractor,
            componentsEventsRepository = componentsEventsRepository,
        )

        viewModel.onCancel()
        advanceUntilIdle()

        coVerify(exactly = 1) { checkoutNavigator.dismiss() }
    }

    @Test
    fun `should handle empty payment methods list`() = runTest {
        every { availablePaymentMethodsUseCase.cache } returns emptyList()

        viewModel = PaymentMethodSelectionViewModel(
            basicOrderInfoInteractor = basicOrderInfoInteractor,
            checkoutNavigator = checkoutNavigator,
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            formatAmountToCurrencyInteractor = formatAmountToCurrencyInteractor,
            componentsEventsRepository = componentsEventsRepository,
        )

        val state = viewModel.state.value
        assertTrue(state.paymentMethods.isEmpty())
    }

    @Test
    fun `should handle payment methods with surcharge`() = runTest {
        val surcharge = Surcharge.PaymentMethodSurcharge(amount = 50)
        val paymentMethod = PrimerComposablePaymentMethod(
            paymentMethodType = "PAYMENT_CARD",
            paymentMethodName = "Card with Surcharge",
            supportedPrimerSessionIntents = listOf(PrimerSessionIntent.CHECKOUT),
            paymentMethodManagerCategories = listOf(PrimerPaymentMethodManagerCategory.NATIVE_UI),
            surcharge = surcharge,
        )

        every { availablePaymentMethodsUseCase.cache } returns listOf(paymentMethod)

        viewModel = PaymentMethodSelectionViewModel(
            basicOrderInfoInteractor = basicOrderInfoInteractor,
            checkoutNavigator = checkoutNavigator,
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            formatAmountToCurrencyInteractor = formatAmountToCurrencyInteractor,
            componentsEventsRepository = componentsEventsRepository,
        )

        val state = viewModel.state.value
        assertEquals(1, state.paymentMethods.size)
        assertEquals(surcharge, state.paymentMethods.first().surcharge)
    }

    // region Analytics Event Tests

    @Test
    fun `init should send CheckoutFlowStarted event`() = runTest {
        viewModel = createViewModel()

        verify(exactly = 1) { componentsEventsRepository.send(EventType.CheckoutFlowStarted, any()) }
    }

    @Test
    fun `onPaymentMethodSelected should send PaymentMethodSelection event with correct paymentMethod`() = runTest {
        viewModel = createViewModel()

        viewModel.onPaymentMethodSelected(PaymentMethodType.PAYMENT_CARD.name)
        advanceUntilIdle()

        verify(exactly = 1) {
            componentsEventsRepository.send(
                match {
                    it is EventType.PaymentMethodSelection &&
                        it.paymentMethod == PaymentMethodType.PAYMENT_CARD.name
                },
                any(),
            )
        }
        coVerify(exactly = 1) { checkoutNavigator.navigateTo(Screen.CardForm) }
    }

    @Test
    fun `onPaymentMethodSelected with GOOGLE_PAY should send event with correct paymentMethod`() = runTest {
        viewModel = createViewModel()

        viewModel.onPaymentMethodSelected(PaymentMethodType.GOOGLE_PAY.name)
        advanceUntilIdle()

        verify(exactly = 1) {
            componentsEventsRepository.send(
                match {
                    it is EventType.PaymentMethodSelection &&
                        it.paymentMethod == PaymentMethodType.GOOGLE_PAY.name
                },
                any(),
            )
        }
        coVerify(exactly = 1) { checkoutNavigator.navigateTo(Screen.NativeUi(PaymentMethodType.GOOGLE_PAY.name)) }
    }

    @Test
    fun `onPaymentMethodSelected with unknown method should send event but not navigate`() = runTest {
        viewModel = createViewModel()

        viewModel.onPaymentMethodSelected("UNKNOWN_PAYMENT_METHOD")
        advanceUntilIdle()

        verify(exactly = 1) {
            componentsEventsRepository.send(
                match {
                    it is EventType.PaymentMethodSelection &&
                        it.paymentMethod == "UNKNOWN_PAYMENT_METHOD"
                },
                any(),
            )
        }
        coVerify(exactly = 0) { checkoutNavigator.navigateTo(any()) }
    }

    // endregion
}
