package io.primer.android.vouchers.retailOutlets.implementation.composer

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.primer.android.PrimerRetailerData
import io.primer.android.RetailOutletsList
import io.primer.android.PrimerSessionIntent
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.paymentmethods.PaymentInputDataValidator
import io.primer.android.paymentmethods.PrimerInitializationData
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.vouchers.retailOutlets.implementation.payment.delegate.RetailOutletsPaymentDelegate
import io.primer.android.vouchers.retailOutlets.implementation.rpc.domain.RetailOutletInteractor
import io.primer.android.vouchers.retailOutlets.implementation.rpc.domain.models.RetailOutlet
import io.primer.android.vouchers.retailOutlets.implementation.tokenization.presentation.RetailOutletsTokenizationDelegate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
internal class RetailOutletsComponentTest {

    private lateinit var component: RetailOutletsComponent
    private lateinit var tokenizationDelegate: RetailOutletsTokenizationDelegate
    private lateinit var paymentDelegate: RetailOutletsPaymentDelegate
    private lateinit var retailOutletsDataValidator: PaymentInputDataValidator<PrimerRetailerData>
    private lateinit var retailOutletInteractor: RetailOutletInteractor
    private lateinit var errorMapperRegistry: ErrorMapperRegistry

    private val testDispatcher = TestCoroutineDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        tokenizationDelegate = mockk(relaxed = true)
        paymentDelegate = mockk(relaxed = true)
        retailOutletsDataValidator = mockk(relaxed = true)
        retailOutletInteractor = mockk()
        errorMapperRegistry = mockk()

        component = RetailOutletsComponent(
            tokenizationDelegate,
            paymentDelegate,
            retailOutletsDataValidator,
            retailOutletInteractor,
            errorMapperRegistry
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `configure calls retailOutletInteractor and completion with success`() = runTest {
        val paymentMethodType = "payment_method_type"
        val sessionIntent: PrimerSessionIntent = mockk()
        val outlets = listOf<RetailOutlet>()
        val retailOutletsList = RetailOutletsList(result = outlets)

        component.start(paymentMethodType, sessionIntent)

        coEvery { retailOutletInteractor(any()) } returns Result.success(outlets)
        val completion: (PrimerInitializationData?, PrimerError?) -> Unit = mockk(relaxed = true)

        component.configure(completion)

        coVerify { completion(retailOutletsList, null) }
    }

    @Test
    fun `configure calls retailOutletInteractor and completion with failure`() = runTest {
        val paymentMethodType = "payment_method_type"
        val sessionIntent: PrimerSessionIntent = mockk()
        val throwable = Exception("error")
        val primerError: PrimerError = mockk()

        component.start(paymentMethodType, sessionIntent)

        coEvery { retailOutletInteractor(any()) } returns Result.failure(throwable)
        every { errorMapperRegistry.getPrimerError(any()) } returns primerError
        val completion: (PrimerInitializationData?, PrimerError?) -> Unit = mockk(relaxed = true)

        component.configure(completion)

        coVerify { completion(null, primerError) }
    }

    @Test
    fun `updateCollectedData emits collectedData and validates raw data`() = runTest {
        val collectedData: PrimerRetailerData = mockk()
        val validationErrors = listOf<PrimerInputValidationError>()

        component = RetailOutletsComponent(
            tokenizationDelegate,
            paymentDelegate,
            retailOutletsDataValidator,
            retailOutletInteractor,
            errorMapperRegistry
        )

        coEvery { retailOutletsDataValidator.validate(any()) } returns validationErrors

        val job = launch {
            component.componentInputValidations.collect {
                assertEquals(validationErrors, it)
            }
        }

        component.updateCollectedData(collectedData)

        job.cancel()
    }
}
