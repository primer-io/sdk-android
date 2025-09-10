package io.primer.android.vouchers.retailOutlets.implementation.composer

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.spyk
import io.primer.android.PrimerRetailerData
import io.primer.android.PrimerSessionIntent
import io.primer.android.RetailOutletsList
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.core.InstantExecutorExtension
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.utils.CoroutineScopeProvider
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.paymentmethods.PaymentInputDataValidator
import io.primer.android.paymentmethods.PrimerInitializationData
import io.primer.android.vouchers.retailOutlets.implementation.payment.delegate.RetailOutletsPaymentDelegate
import io.primer.android.vouchers.retailOutlets.implementation.rpc.domain.RetailOutletInteractor
import io.primer.android.vouchers.retailOutlets.implementation.rpc.domain.models.RetailOutlet
import io.primer.android.vouchers.retailOutlets.implementation.tokenization.presentation.RetailOutletsTokenizationDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
internal class RetailOutletsComponentTest {
    private lateinit var component: RetailOutletsComponent
    private lateinit var tokenizationDelegate: RetailOutletsTokenizationDelegate
    private lateinit var paymentDelegate: RetailOutletsPaymentDelegate
    private lateinit var retailOutletsDataValidator: PaymentInputDataValidator<PrimerRetailerData>
    private lateinit var retailOutletInteractor: RetailOutletInteractor
    private lateinit var errorMapperRegistry: ErrorMapperRegistry

    @BeforeEach
    fun setUp() {
        tokenizationDelegate = mockk(relaxed = true)
        paymentDelegate = mockk(relaxed = true)
        retailOutletsDataValidator = mockk(relaxed = true)
        retailOutletInteractor = mockk()
        errorMapperRegistry = mockk()
        DISdkContext.headlessSdkContainer =
            mockk<SdkContainer>(relaxed = true).also { sdkContainer ->
                val cont =
                    spyk<DependencyContainer>().also { container ->
                        container.registerFactory<CoroutineScopeProvider> {
                            object : CoroutineScopeProvider {
                                override val scope: CoroutineScope
                                    get() = TestScope()
                            }
                        }
                    }
                every { sdkContainer.containers }
                    .returns(ConcurrentHashMap(mutableMapOf(cont::class.simpleName.orEmpty() to cont)))
            }

        component = RetailOutletsComponent(
            tokenizationDelegate,
            paymentDelegate,
            retailOutletsDataValidator,
            retailOutletInteractor,
            errorMapperRegistry,
        )
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `configure calls retailOutletInteractor and completion with success`() {
        val paymentMethodType = "payment_method_type"
        val sessionIntent: PrimerSessionIntent = mockk()
        val outlets = listOf<RetailOutlet>()
        val retailOutletsList = RetailOutletsList(result = outlets)

        runTest {
            component.start(paymentMethodType, sessionIntent)
        }

        coEvery { retailOutletInteractor(any()) } returns Result.success(outlets)
        val completion: (PrimerInitializationData?, PrimerError?) -> Unit = mockk(relaxed = true)

        runTest {
            component.configure(completion)
        }

        coVerify { completion(retailOutletsList, null) }
    }

    @Test
    fun `configure calls retailOutletInteractor and completion with failure`() {
        val paymentMethodType = "payment_method_type"
        val sessionIntent: PrimerSessionIntent = mockk()
        val throwable = Exception("error")
        val primerError: PrimerError = mockk()
        runTest {
            component.start(paymentMethodType, sessionIntent)
        }

        coEvery { retailOutletInteractor(any()) } returns Result.failure(throwable)
        every { errorMapperRegistry.getPrimerError(any()) } returns primerError
        val completion: (PrimerInitializationData?, PrimerError?) -> Unit = mockk(relaxed = true)

        runTest {
            component.configure(completion)
        }

        coVerify { completion(null, primerError) }
    }

    @Test
    fun `updateCollectedData emits collectedData and validates raw data`() =
        runTest {
            val collectedData: PrimerRetailerData = mockk()
            val validationErrors = listOf<PrimerInputValidationError>()

            component =
                RetailOutletsComponent(
                    tokenizationDelegate,
                    paymentDelegate,
                    retailOutletsDataValidator,
                    retailOutletInteractor,
                    errorMapperRegistry,
                )

            coEvery { retailOutletsDataValidator.validate(any()) } returns validationErrors

            val job =
                launch {
                    component.componentInputValidations.collect {
                        assertEquals(validationErrors, it)
                    }
                }

            component.updateCollectedData(collectedData)

            job.cancel()
        }
}
