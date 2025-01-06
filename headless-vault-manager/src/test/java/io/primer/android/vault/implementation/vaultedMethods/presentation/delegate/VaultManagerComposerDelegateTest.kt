package io.primer.android.vault.implementation.vaultedMethods.presentation.delegate

import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.android.paymentmethods.core.composer.VaultedPaymentMethodComponent
import io.primer.android.paymentmethods.core.composer.provider.VaultedPaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.composer.registry.VaultedPaymentMethodComposerRegistry
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationFactoryRegistry
import io.primer.android.payments.core.create.domain.model.PaymentDecision
import io.primer.android.payments.core.helpers.PaymentMethodPaymentDelegate
import io.primer.android.vault.implementation.composer.presentation.DefaultVaultedPaymentMethodComponent
import kotlinx.coroutines.test.runTest
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

    private lateinit var delegate: VaultManagerComposerDelegate

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
    }

    @Test
    fun `handlePaymentMethod() will unregister current composer and resolve correct composer if registered`() {
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
        runTest {
            delegate.handlePaymentMethod(paymentMethodToken)
        }

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
        )
    }
}
