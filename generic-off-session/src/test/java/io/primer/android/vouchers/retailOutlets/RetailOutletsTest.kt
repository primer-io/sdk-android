package io.primer.android.vouchers.retailOutlets

import android.content.Context
import io.mockk.Called
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.assets.ui.registry.BrandRegistry
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.paymentmethods.PaymentMethodCheckerRegistry
import io.primer.android.paymentmethods.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.composer.provider.VaultedPaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationFactoryRegistry
import io.primer.android.vouchers.retailOutlets.implementation.composer.presentation.provider.RetailOutletsComposerProviderFactory
import io.primer.android.vouchers.retailOutlets.implementation.composer.ui.assets.RetailOutletsBrand
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class RetailOutletsTest {
    private lateinit var retailOutlets: RetailOutlets
    private val paymentMethodType = "RETAIL_OUTLETS"

    @BeforeEach
    fun setUp() {
        retailOutlets = RetailOutlets(paymentMethodType)
    }

    @Test
    fun `type should return paymentMethodType`() {
        assertEquals(paymentMethodType, retailOutlets.type)
    }

    @Test
    fun `canBeVaulted should return false`() {
        assertFalse(retailOutlets.canBeVaulted)
    }

    @Test
    fun `module should initialize without errors`() {
        val context = mockk<Context>()
        val configuration = mockk<ConfigurationData>()
        retailOutlets.module.initialize(context, configuration)
        // no exception thrown means the test passed
    }

    @Test
    fun `module should register PaymentMethodDescriptorFactory`() {
        val descriptorFactoryRegistry = mockk<PaymentMethodDescriptorFactoryRegistry>(relaxed = true)
        retailOutlets.module.registerPaymentMethodDescriptorFactory(descriptorFactoryRegistry)
        verify { descriptorFactoryRegistry.register(paymentMethodType, any<RetailOutletsDescriptorFactory>()) }
    }

    @Test
    fun `module should register PaymentMethodProviderFactory`() {
        val providerFactoryRegistry = mockk<PaymentMethodProviderFactoryRegistry>(relaxed = true)
        retailOutlets.module.registerPaymentMethodProviderFactory(providerFactoryRegistry)
        verify { providerFactoryRegistry.register(paymentMethodType, RetailOutletsComposerProviderFactory::class.java) }
    }

    @Test
    fun `module should register BrandProvider`() {
        val brandRegistry = mockk<BrandRegistry>(relaxed = true)
        retailOutlets.module.registerBrandProvider(brandRegistry)
        verify { brandRegistry.register(paymentMethodType, any<RetailOutletsBrand>()) }
    }

    @Test
    fun `module should not register PaymentMethodCheckers`() {
        val checkerRegistry = mockk<PaymentMethodCheckerRegistry>(relaxed = true)
        retailOutlets.module.registerPaymentMethodCheckers(checkerRegistry)
        verify { checkerRegistry wasNot Called }
    }

    @Test
    fun `module should not register SavedPaymentMethodProviderFactory`() {
        val providerFactoryRegistry = mockk<VaultedPaymentMethodProviderFactoryRegistry>(relaxed = true)
        retailOutlets.module.registerSavedPaymentMethodProviderFactory(providerFactoryRegistry)
        verify { providerFactoryRegistry wasNot Called }
    }

    @Test
    fun `module should not register PaymentMethodNavigationFactory`() {
        val navigationFactoryRegistry = mockk<PaymentMethodNavigationFactoryRegistry>(relaxed = true)
        retailOutlets.module.registerPaymentMethodNavigationFactory(navigationFactoryRegistry)
        verify { navigationFactoryRegistry wasNot Called }
    }

    @Test
    fun `module should not register ErrorMappers`() {
        val errorMapperRegistry = mockk<ErrorMapperRegistry>(relaxed = true)
        retailOutlets.module.registerErrorMappers(errorMapperRegistry)
        verify { errorMapperRegistry wasNot Called }
    }
}
