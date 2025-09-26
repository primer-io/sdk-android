package io.primer.android.vault.implementation.vaultedMethods.presentation.delegate

import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.core.utils.CoroutineScopeProvider
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.android.paymentmethods.core.composer.VaultedPaymentMethodComponent
import io.primer.android.paymentmethods.core.composer.provider.VaultedPaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.composer.registry.VaultedPaymentMethodComposerRegistry
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationFactoryRegistry
import io.primer.android.payments.core.create.domain.model.PaymentDecision
import io.primer.android.payments.core.helpers.PaymentMethodPaymentDelegate
import io.primer.android.vault.implementation.composer.presentation.DefaultVaultedPaymentMethodComponent
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class VaultManagerComposerDelegateTest {
    @RelaxedMockK
    internal lateinit var paymentMethodNavigationFactoryRegistry: PaymentMethodNavigationFactoryRegistry

    @RelaxedMockK
    internal lateinit var composerRegistry: VaultedPaymentMethodComposerRegistry

    @RelaxedMockK
    internal lateinit var providerFactoryRegistry: VaultedPaymentMethodProviderFactoryRegistry

    @RelaxedMockK
    internal lateinit var context: Context

    @RelaxedMockK
    internal lateinit var coroutineScopeProvider: CoroutineScopeProvider

    private lateinit var delegate: VaultManagerComposerDelegate

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        every { coroutineScopeProvider.scope } returns TestScope()
    }

    @AfterEach
    fun tearDown() {
        confirmVerified(paymentMethodNavigationFactoryRegistry, composerRegistry, providerFactoryRegistry, context)
        clearAllMocks()
    }

    @Test
    fun `handlePaymentMethod() will cancel current composer if registered`() {
        val paymentDelegate: PaymentMethodPaymentDelegate = mockk(relaxed = true)
        delegate = getDelegate(paymentDelegate = paymentDelegate)

        val paymentMethodToken = mockk<PrimerPaymentMethodTokenData>(relaxed = true)
        val composer = mockk<VaultedPaymentMethodComponent>(relaxed = true)

        coEvery {
            paymentDelegate.handlePaymentMethodToken(
                any(),
                any(),
            )
        } returns Result.success(mockk<PaymentDecision>(relaxed = true))
        every { providerFactoryRegistry.create(any(), any()) } returns composer
        every { composerRegistry[any()] } returns composer
        runTest {
            delegate.handlePaymentMethod(paymentMethodToken)
        }

        verify(exactly = 1) { composerRegistry[any()] }
        verify { composer.cancel() }
        verify { composerRegistry.unregister(any()) }
        verify { providerFactoryRegistry.create(any(), any()) }
        verify { composerRegistry.register(any(), composer) }
    }

    @Test
    fun `handlePaymentMethod() will unregister current composer and resolve correct composer if registered`() {
        val paymentDelegate: PaymentMethodPaymentDelegate = mockk(relaxed = true)
        delegate = getDelegate(paymentDelegate = paymentDelegate)

        val paymentMethodToken = mockk<PrimerPaymentMethodTokenData>(relaxed = true)
        val composer = mockk<VaultedPaymentMethodComponent>(relaxed = true) {
            every { cancel() } just Runs
        }

        every { composerRegistry[any()] } returns null
        coEvery {
            paymentDelegate.handlePaymentMethodToken(
                any(),
                any(),
            )
        } returns Result.success(mockk<PaymentDecision>(relaxed = true))
        every { providerFactoryRegistry.create(any(), any()) } returns composer
        runTest {
            delegate.handlePaymentMethod(paymentMethodToken)
        }

        verify(exactly = 1) { composerRegistry[any()] }
        verify { composerRegistry.unregister(any()) }
        verify { providerFactoryRegistry.create(any(), any()) }
        verify { composerRegistry.register(any(), composer) }
    }

    @Test
    fun `handlePaymentMethod() will unregister current composer and resolve to default composer if not registered`() {
        val paymentDelegate: PaymentMethodPaymentDelegate = mockk(relaxed = true)
        delegate = getDelegate(paymentDelegate = paymentDelegate)

        val paymentMethodToken = mockk<PrimerPaymentMethodTokenData>(relaxed = true)

        coEvery {
            paymentDelegate.handlePaymentMethodToken(
                any(),
                any(),
            )
        } returns Result.success(mockk<PaymentDecision>(relaxed = true))
        every { providerFactoryRegistry.create(any(), any()) } returns null
        runTest {
            delegate.handlePaymentMethod(paymentMethodToken)
        }

        verify(exactly = 1) { composerRegistry[any()] }
        verify { composerRegistry.unregister(any()) }
        verify { providerFactoryRegistry.create(any(), any()) }
        verify { composerRegistry.register(any(), ofType(DefaultVaultedPaymentMethodComponent::class)) }
    }

    @Test
    fun `handlePaymentMethod() will unregister current composer and resolve to default composer if not registered and return error result if handlePaymentMethodToken fails`() {
        val paymentDelegate: PaymentMethodPaymentDelegate = mockk(relaxed = true)
        delegate = getDelegate(paymentDelegate = paymentDelegate)

        val paymentMethodToken = mockk<PrimerPaymentMethodTokenData>(relaxed = true)
        val exception = mockk<Exception>(relaxed = true)

        coEvery {
            paymentDelegate.handlePaymentMethodToken(
                any(),
                any(),
            )
        } returns Result.failure(exception)
        every { providerFactoryRegistry.create(any(), any()) } returns null
        runTest {
            delegate.handlePaymentMethod(paymentMethodToken)
        }

        verify(exactly = 1) { composerRegistry[any()] }
        verify { composerRegistry.unregister(any()) }
        verify { providerFactoryRegistry.create(any(), any()) }
        verify { composerRegistry.register(any(), ofType(DefaultVaultedPaymentMethodComponent::class)) }
        coVerify { paymentDelegate.handleError(exception) }
    }

    private fun getDelegate(paymentDelegate: PaymentMethodPaymentDelegate): VaultManagerComposerDelegate {
        return VaultManagerComposerDelegate(
            paymentMethodNavigationFactoryRegistry = paymentMethodNavigationFactoryRegistry,
            composerRegistry = composerRegistry,
            providerFactoryRegistry = providerFactoryRegistry,
            context = context,
            paymentDelegateProvider = { paymentDelegate },
            headlessScopeProvider = coroutineScopeProvider,
        )
    }
}
