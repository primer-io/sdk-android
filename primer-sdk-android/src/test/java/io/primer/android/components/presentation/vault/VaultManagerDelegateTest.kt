package io.primer.android.components.presentation.vault

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.slot
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.components.domain.payments.vault.HeadlessVaultedPaymentMethodInteractor
import io.primer.android.components.domain.payments.vault.HeadlessVaultedPaymentMethodsExchangeInteractor
import io.primer.android.components.domain.payments.vault.HeadlessVaultedPaymentMethodsInteractor
import io.primer.android.components.domain.payments.vault.model.card.PrimerVaultedCardAdditionalData
import io.primer.android.components.domain.payments.vault.validation.additionalData.VaultedPaymentMethodAdditionalDataValidatorRegistry
import io.primer.android.components.domain.payments.vault.validation.additionalData.card.VaultedCardAdditionalDataValidator
import io.primer.android.components.domain.payments.vault.validation.resolvers.VaultManagerInitValidationRulesResolver
import io.primer.android.components.ui.navigation.Navigator
import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import io.primer.android.domain.payments.create.CreatePaymentInteractor
import io.primer.android.domain.payments.methods.VaultedPaymentMethodsDeleteInteractor
import io.primer.android.domain.payments.resume.ResumePaymentInteractor
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals

internal class VaultManagerDelegateTest {

    @RelaxedMockK
    internal lateinit var initValidationRulesResolver: VaultManagerInitValidationRulesResolver

    @RelaxedMockK
    internal lateinit var vaultedPaymentMethodsInteractor:
        HeadlessVaultedPaymentMethodsInteractor

    @RelaxedMockK
    internal lateinit var vaultedPaymentMethodsDeleteInteractor:
        VaultedPaymentMethodsDeleteInteractor

    @RelaxedMockK
    internal lateinit var vaultedPaymentMethodsExchangeInteractor:
        HeadlessVaultedPaymentMethodsExchangeInteractor

    @RelaxedMockK
    internal lateinit var headlessVaultedPaymentMethodInteractor:
        HeadlessVaultedPaymentMethodInteractor

    @RelaxedMockK
    internal lateinit var createPaymentInteractor: CreatePaymentInteractor

    @RelaxedMockK
    internal lateinit var resumePaymentInteractor: ResumePaymentInteractor

    @RelaxedMockK
    internal lateinit var analyticsInteractor: AnalyticsInteractor

    @RelaxedMockK
    internal lateinit var vaultedPaymentMethodAdditionalDataValidatorRegistry:
        VaultedPaymentMethodAdditionalDataValidatorRegistry

    @RelaxedMockK
    internal lateinit var navigator: Navigator

    private lateinit var delegate: VaultManagerDelegate

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        delegate = VaultManagerDelegate(
            initValidationRulesResolver,
            vaultedPaymentMethodsInteractor,
            vaultedPaymentMethodsDeleteInteractor,
            vaultedPaymentMethodsExchangeInteractor,
            headlessVaultedPaymentMethodInteractor,
            createPaymentInteractor,
            resumePaymentInteractor,
            analyticsInteractor,
            vaultedPaymentMethodAdditionalDataValidatorRegistry,
            navigator
        )
    }

    @Test
    fun `fetchVaultedPaymentMethods() should return Result containing list of PrimerVaultedPaymentMethodData when HeadlessVaultedPaymentMethodsInteractor invoke is successful`() {
        val list = mockk<List<PrimerVaultedPaymentMethod>>()

        coEvery { vaultedPaymentMethodsInteractor(any()) }.returns(Result.success(list))
        runTest {
            assertEquals(Result.success(list), delegate.fetchVaultedPaymentMethods())
        }

        coVerify { vaultedPaymentMethodsInteractor(any()) }
    }

    @Test
    fun `fetchVaultedPaymentMethods() should return Result with Throwable when HeadlessVaultedPaymentMethodsInteractor invoke failed`() {
        val exception = mockk<Exception>()

        coEvery { vaultedPaymentMethodsInteractor(any()) }.returns(Result.failure(exception))
        runTest {
            assertEquals(delegate.fetchVaultedPaymentMethods(), Result.failure(exception))
        }

        coVerify { vaultedPaymentMethodsInteractor(any()) }
    }

    @Test
    fun `fetchVaultedPaymentMethods() should add correct analytics event when called`() {
        val result = mockk<Result<List<PrimerVaultedPaymentMethod>>>()

        coEvery { vaultedPaymentMethodsInteractor(any()) }.returns(result)
        coEvery { analyticsInteractor(any()) }.returns(flowOf(Unit))
        runTest {
            delegate.fetchVaultedPaymentMethods()
        }

        val analyticsEvent = slot<SdkFunctionParams>()

        coVerify { analyticsInteractor(capture(analyticsEvent)) }

        assertEquals(VaultManagerDelegate.ANALYTICS_EVENT_FETCH, analyticsEvent.captured.name)
    }

    @Test
    fun `deletePaymentMethod() should return Result of Unit when VaultedPaymentMethodsDeleteInteractor invoke is successful`() {
        val vaultedPaymentMethodId = UUID.randomUUID().toString()
        val primerVaultedPaymentMethod = mockk<PrimerVaultedPaymentMethod>(relaxed = true)

        coEvery { headlessVaultedPaymentMethodInteractor(any()) }.returns(
            Result.success(primerVaultedPaymentMethod)
        )
        coEvery {
            vaultedPaymentMethodsDeleteInteractor(any())
        }.returns(Result.success(vaultedPaymentMethodId))

        runTest {
            assertEquals(
                Result.success(Unit),
                delegate.deletePaymentMethod(vaultedPaymentMethodId)
            )
        }

        coVerify { headlessVaultedPaymentMethodInteractor(any()) }
        coVerify { vaultedPaymentMethodsDeleteInteractor(any()) }
    }

    @Test
    fun `deletePaymentMethod() should return Result with Throwable when VaultedPaymentMethodsDeleteInteractor invoke failed`() {
        val vaultedPaymentMethodId = UUID.randomUUID().toString()
        val exception = mockk<Exception>(relaxed = true)
        val primerVaultedPaymentMethod = mockk<PrimerVaultedPaymentMethod>(relaxed = true)

        coEvery { headlessVaultedPaymentMethodInteractor(any()) }.returns(
            Result.success(primerVaultedPaymentMethod)
        )

        coEvery { vaultedPaymentMethodsDeleteInteractor(any()) }.returns(Result.failure(exception))
        runTest {
            assertEquals(
                Result.failure(exception),
                delegate.deletePaymentMethod(vaultedPaymentMethodId)
            )
        }

        coVerify { headlessVaultedPaymentMethodInteractor(any()) }
        coVerify { vaultedPaymentMethodsDeleteInteractor(any()) }
    }

    @Test
    fun `deletePaymentMethod() should add correct analytics event when called`() {
        val vaultedPaymentMethodId = UUID.randomUUID().toString()
        val result = mockk<Result<String>>()
        val primerVaultedPaymentMethod = mockk<PrimerVaultedPaymentMethod>(relaxed = true)

        coEvery { headlessVaultedPaymentMethodInteractor(any()) }.returns(
            Result.success(primerVaultedPaymentMethod)
        )

        coEvery { vaultedPaymentMethodsDeleteInteractor(any()) }.returns(result)
        coEvery { analyticsInteractor(any()) }.returns(flowOf(Unit))

        runTest {
            delegate.deletePaymentMethod(vaultedPaymentMethodId)
        }

        val analyticsEvent = slot<SdkFunctionParams>()

        coVerify { analyticsInteractor(capture(analyticsEvent)) }

        assertEquals(VaultManagerDelegate.ANALYTICS_EVENT_DELETE, analyticsEvent.captured.name)
    }

    @Test
    fun `validate() should return Result containing list of PrimerValidationError when VaultedPaymentMethodAdditionalDataValidator validate returns errors`() {
        val vaultedPaymentMethodId = UUID.randomUUID().toString()
        val additionalData = mockk<PrimerVaultedCardAdditionalData>()
        every { additionalData.cvv }.returns("13")

        val primerVaultedPaymentMethod = mockk<PrimerVaultedPaymentMethod>(relaxed = true)

        coEvery { headlessVaultedPaymentMethodInteractor(any()) }.returns(
            Result.success(primerVaultedPaymentMethod)
        )
        coEvery {
            vaultedPaymentMethodAdditionalDataValidatorRegistry.getValidator(any())
        }.returns(VaultedCardAdditionalDataValidator())

        runTest {
            val errors = delegate.validate(vaultedPaymentMethodId, additionalData).getOrNull()
            assertEquals("invalid-cvv", errors?.first()?.errorId)
            assertEquals(
                "The length of the CVV does not match the required length.",
                errors?.first()?.description
            )
        }

        coVerify { headlessVaultedPaymentMethodInteractor(any()) }
        coVerify { vaultedPaymentMethodAdditionalDataValidatorRegistry.getValidator(any()) }
    }

    @Test
    fun `validate() should return Result with Throwable when HeadlessVaultedPaymentMethodInteractor invoke failed`() {
        val vaultedPaymentMethodId = UUID.randomUUID().toString()
        val exception = mockk<Exception>(relaxed = true)
        val additionalData = mockk<PrimerVaultedCardAdditionalData>()

        coEvery { headlessVaultedPaymentMethodInteractor(any()) }.returns(
            Result.failure(exception)
        )

        runTest {
            assertEquals(
                Result.failure(exception),
                delegate.validate(vaultedPaymentMethodId, additionalData)
            )
        }

        coVerify { headlessVaultedPaymentMethodInteractor(any()) }
    }

    @Test
    fun `validate() should add correct analytics event when called`() {
        val vaultedPaymentMethodId = UUID.randomUUID().toString()
        val primerVaultedPaymentMethod = mockk<PrimerVaultedPaymentMethod>(relaxed = true)
        val additionalData = mockk<PrimerVaultedCardAdditionalData>()
        every { additionalData.cvv }.returns("13")

        coEvery { headlessVaultedPaymentMethodInteractor(any()) }.returns(
            Result.success(primerVaultedPaymentMethod)
        )

        coEvery {
            vaultedPaymentMethodAdditionalDataValidatorRegistry.getValidator(any())
        }.returns(VaultedCardAdditionalDataValidator())
        coEvery { analyticsInteractor(any()) }.returns(flowOf(Unit))

        runTest {
            delegate.validate(vaultedPaymentMethodId, additionalData)
        }

        val analyticsEvent = slot<SdkFunctionParams>()

        coVerify { analyticsInteractor(capture(analyticsEvent)) }

        assertEquals(VaultManagerDelegate.ANALYTICS_EVENT_VALIDATE, analyticsEvent.captured.name)
    }

    @Test
    fun `startPaymentFlow() should return Result of Unit when HeadlessVaultedPaymentMethodsExchangeInteractor invoke is successful`() {
        val vaultedPaymentMethodId = UUID.randomUUID().toString()
        val additionalData = mockk<PrimerVaultedCardAdditionalData>()

        val primerVaultedPaymentMethod = mockk<PrimerVaultedPaymentMethod>(relaxed = true)
        val paymentMethodTokenInternal = mockk<PaymentMethodTokenInternal>(relaxed = true)

        coEvery { headlessVaultedPaymentMethodInteractor(any()) }.returns(
            Result.success(primerVaultedPaymentMethod)
        )
        coEvery {
            vaultedPaymentMethodsExchangeInteractor(any())
        }.returns(flowOf(paymentMethodTokenInternal))

        runTest {
            delegate.startPaymentFlow(vaultedPaymentMethodId, additionalData)
        }

        coVerify { headlessVaultedPaymentMethodInteractor(any()) }
        coVerify { vaultedPaymentMethodsExchangeInteractor(any()) }
    }

    @Test
    fun `startPaymentFlow() should return Result with Throwable when HeadlessVaultedPaymentMethodInteractor invoke failed`() {
        val vaultedPaymentMethodId = UUID.randomUUID().toString()
        val exception = mockk<Exception>(relaxed = true)
        val additionalData = mockk<PrimerVaultedCardAdditionalData>()

        coEvery { headlessVaultedPaymentMethodInteractor(any()) }.returns(
            Result.failure(exception)
        )

        runTest {
            assertEquals(
                Result.failure(exception),
                delegate.validate(vaultedPaymentMethodId, additionalData)
            )
        }

        coVerify { headlessVaultedPaymentMethodInteractor(any()) }
    }

    @Test
    fun `startPaymentFlow() should add correct analytics event when called`() {
        val vaultedPaymentMethodId = UUID.randomUUID().toString()
        val paymentMethodTokenInternal = mockk<PaymentMethodTokenInternal>(relaxed = true)

        val primerVaultedPaymentMethod = mockk<PrimerVaultedPaymentMethod>(relaxed = true)
        val additionalData = mockk<PrimerVaultedCardAdditionalData>()

        coEvery { headlessVaultedPaymentMethodInteractor(any()) }.returns(
            Result.success(primerVaultedPaymentMethod)
        )

        coEvery {
            vaultedPaymentMethodsExchangeInteractor(any())
        }.returns(flowOf(paymentMethodTokenInternal))

        coEvery { analyticsInteractor(any()) }.returns(flowOf(Unit))

        runTest {
            delegate.startPaymentFlow(vaultedPaymentMethodId, additionalData)
        }

        val analyticsEvent = slot<SdkFunctionParams>()

        coVerify { analyticsInteractor(capture(analyticsEvent)) }

        assertEquals(
            VaultManagerDelegate.ANALYTICS_EVENT_START_PAYMENT_FLOW,
            analyticsEvent.captured.name
        )
    }
}
