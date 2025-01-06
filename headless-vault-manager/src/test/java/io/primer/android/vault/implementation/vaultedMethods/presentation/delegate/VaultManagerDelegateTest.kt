package io.primer.android.vault.implementation.vaultedMethods.presentation.delegate

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.components.domain.payments.vault.model.card.PrimerVaultedCardAdditionalData
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.payments.core.tokenization.data.model.PaymentMethodTokenInternal
import io.primer.android.payments.core.tokenization.data.model.toPaymentMethodToken
import io.primer.android.vault.implementation.vaultedMethods.domain.FetchVaultedPaymentMethodsInteractor
import io.primer.android.vault.implementation.vaultedMethods.domain.FindVaultedPaymentMethodInteractor
import io.primer.android.vault.implementation.vaultedMethods.domain.VaultedPaymentMethodsDeleteInteractor
import io.primer.android.vault.implementation.vaultedMethods.domain.VaultedPaymentMethodsExchangeInteractor
import io.primer.android.vault.implementation.vaultedMethods.domain.validation.additionalData.VaultedPaymentMethodAdditionalDataValidatorRegistry
import io.primer.android.vault.implementation.vaultedMethods.domain.validation.additionalData.card.VaultedCardAdditionalDataValidator
import io.primer.android.vault.implementation.vaultedMethods.domain.validation.resolvers.VaultManagerInitValidationRulesResolver
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
internal class VaultManagerDelegateTest {
    @RelaxedMockK
    internal lateinit var initValidationRulesResolver: VaultManagerInitValidationRulesResolver

    @RelaxedMockK
    internal lateinit var vaultedPaymentMethodsInteractor: FetchVaultedPaymentMethodsInteractor

    @RelaxedMockK
    internal lateinit var vaultedPaymentMethodsDeleteInteractor: VaultedPaymentMethodsDeleteInteractor

    @RelaxedMockK
    internal lateinit var vaultedPaymentMethodsExchangeInteractor: VaultedPaymentMethodsExchangeInteractor

    @RelaxedMockK
    internal lateinit var findVaultedPaymentMethodInteractor: FindVaultedPaymentMethodInteractor

    @RelaxedMockK
    internal lateinit var analyticsInteractor: AnalyticsInteractor

    @RelaxedMockK
    internal lateinit var errorMapperRegistry: ErrorMapperRegistry

    @RelaxedMockK
    internal lateinit var vaultedPaymentMethodAdditionalDataValidatorRegistry:
        VaultedPaymentMethodAdditionalDataValidatorRegistry

    private lateinit var delegate: VaultManagerDelegate

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        delegate =
            VaultManagerDelegate(
                initValidationRulesResolver = initValidationRulesResolver,
                vaultedPaymentMethodsInteractor = vaultedPaymentMethodsInteractor,
                vaultedPaymentMethodsDeleteInteractor = vaultedPaymentMethodsDeleteInteractor,
                vaultedPaymentMethodsExchangeInteractor = vaultedPaymentMethodsExchangeInteractor,
                findVaultedPaymentMethodInteractor = findVaultedPaymentMethodInteractor,
                analyticsInteractor = analyticsInteractor,
                errorMapperRegistry = errorMapperRegistry,
                vaultedPaymentMethodAdditionalDataValidatorRegistry =
                vaultedPaymentMethodAdditionalDataValidatorRegistry,
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
        coEvery { analyticsInteractor(any()) }.returns(Result.success(Unit))
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

        coEvery { findVaultedPaymentMethodInteractor(any()) }.returns(
            Result.success(primerVaultedPaymentMethod),
        )
        coEvery {
            vaultedPaymentMethodsDeleteInteractor(any())
        }.returns(Result.success(vaultedPaymentMethodId))

        runTest {
            assertEquals(
                Result.success(Unit),
                delegate.deletePaymentMethod(vaultedPaymentMethodId),
            )
        }

        coVerify { findVaultedPaymentMethodInteractor(any()) }
        coVerify { vaultedPaymentMethodsDeleteInteractor(any()) }
    }

    @Test
    fun `deletePaymentMethod() should return Result with Throwable when VaultedPaymentMethodsDeleteInteractor invoke failed`() {
        val vaultedPaymentMethodId = UUID.randomUUID().toString()
        val exception = mockk<Exception>(relaxed = true)
        val primerVaultedPaymentMethod = mockk<PrimerVaultedPaymentMethod>(relaxed = true)

        coEvery { findVaultedPaymentMethodInteractor(any()) }.returns(
            Result.success(primerVaultedPaymentMethod),
        )

        coEvery { vaultedPaymentMethodsDeleteInteractor(any()) }.returns(Result.failure(exception))
        runTest {
            assertEquals(
                Result.failure(exception),
                delegate.deletePaymentMethod(vaultedPaymentMethodId),
            )
        }

        coVerify { findVaultedPaymentMethodInteractor(any()) }
        coVerify { vaultedPaymentMethodsDeleteInteractor(any()) }
    }

    @Test
    fun `deletePaymentMethod() should add correct analytics event when called`() {
        val vaultedPaymentMethodId = UUID.randomUUID().toString()
        val result = mockk<Result<String>>()
        val primerVaultedPaymentMethod = mockk<PrimerVaultedPaymentMethod>(relaxed = true)

        coEvery { findVaultedPaymentMethodInteractor(any()) }.returns(
            Result.success(primerVaultedPaymentMethod),
        )

        coEvery { vaultedPaymentMethodsDeleteInteractor(any()) }.returns(result)
        coEvery { analyticsInteractor(any()) }.returns(Result.success(Unit))

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

        coEvery { findVaultedPaymentMethodInteractor(any()) }.returns(
            Result.success(primerVaultedPaymentMethod),
        )
        coEvery {
            vaultedPaymentMethodAdditionalDataValidatorRegistry.getValidator(any())
        }.returns(VaultedCardAdditionalDataValidator())

        runTest {
            val errors = delegate.validate(vaultedPaymentMethodId, additionalData).getOrNull()
            assertEquals("invalid-cvv", errors?.first()?.errorId)
            assertEquals(
                "The length of the CVV does not match the required length.",
                errors?.first()?.description,
            )
        }

        coVerify { findVaultedPaymentMethodInteractor(any()) }
        coVerify { vaultedPaymentMethodAdditionalDataValidatorRegistry.getValidator(any()) }
    }

    @Test
    fun `validate() should return Result with Throwable when HeadlessVaultedPaymentMethodInteractor invoke failed`() {
        val vaultedPaymentMethodId = UUID.randomUUID().toString()
        val exception = mockk<Exception>(relaxed = true)
        val additionalData = mockk<PrimerVaultedCardAdditionalData>()

        coEvery { findVaultedPaymentMethodInteractor(any()) }.returns(
            Result.failure(exception),
        )

        runTest {
            assertEquals(
                Result.failure(exception),
                delegate.validate(vaultedPaymentMethodId, additionalData),
            )
        }

        coVerify { findVaultedPaymentMethodInteractor(any()) }
    }

    @Test
    fun `validate() should add correct analytics event when called`() {
        val vaultedPaymentMethodId = UUID.randomUUID().toString()
        val primerVaultedPaymentMethod = mockk<PrimerVaultedPaymentMethod>(relaxed = true)
        val additionalData = mockk<PrimerVaultedCardAdditionalData>()
        every { additionalData.cvv }.returns("13")

        coEvery { findVaultedPaymentMethodInteractor(any()) }.returns(
            Result.success(primerVaultedPaymentMethod),
        )

        coEvery {
            vaultedPaymentMethodAdditionalDataValidatorRegistry.getValidator(any())
        }.returns(VaultedCardAdditionalDataValidator())
        coEvery { analyticsInteractor(any()) }.returns(Result.success(Unit))

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

        coEvery { findVaultedPaymentMethodInteractor(any()) }.returns(
            Result.success(primerVaultedPaymentMethod),
        )
        coEvery {
            vaultedPaymentMethodsExchangeInteractor(any())
        }.returns(Result.success(paymentMethodTokenInternal.toPaymentMethodToken()))

        runTest {
            delegate.startPaymentFlow(vaultedPaymentMethodId, additionalData)
        }

        coVerify { findVaultedPaymentMethodInteractor(any()) }
        coVerify { vaultedPaymentMethodsExchangeInteractor(any()) }
    }

    @Test
    fun `startPaymentFlow() should return Result with Throwable when HeadlessVaultedPaymentMethodInteractor invoke failed`() {
        val vaultedPaymentMethodId = UUID.randomUUID().toString()
        val exception = mockk<Exception>(relaxed = true)
        val additionalData = mockk<PrimerVaultedCardAdditionalData>()

        coEvery { findVaultedPaymentMethodInteractor(any()) }.returns(
            Result.failure(exception),
        )

        runTest {
            assertEquals(
                Result.failure(exception),
                delegate.validate(vaultedPaymentMethodId, additionalData),
            )
        }

        coVerify { findVaultedPaymentMethodInteractor(any()) }
    }

    @Test
    fun `startPaymentFlow() should add correct analytics event when called`() {
        val vaultedPaymentMethodId = UUID.randomUUID().toString()
        val paymentMethodTokenInternal = mockk<PaymentMethodTokenInternal>(relaxed = true)

        val primerVaultedPaymentMethod = mockk<PrimerVaultedPaymentMethod>(relaxed = true)
        val additionalData = mockk<PrimerVaultedCardAdditionalData>()

        coEvery { findVaultedPaymentMethodInteractor(any()) }.returns(
            Result.success(primerVaultedPaymentMethod),
        )

        coEvery {
            vaultedPaymentMethodsExchangeInteractor(any())
        }.returns(Result.success(paymentMethodTokenInternal.toPaymentMethodToken()))

        coEvery { analyticsInteractor(any()) }.returns(Result.success(Unit))

        runTest {
            delegate.startPaymentFlow(vaultedPaymentMethodId, additionalData)
        }

        val analyticsEvent = slot<SdkFunctionParams>()

        coVerify { analyticsInteractor(capture(analyticsEvent)) }

        assertEquals(
            VaultManagerDelegate.ANALYTICS_EVENT_START_PAYMENT_FLOW,
            analyticsEvent.captured.name,
        )
    }
}
