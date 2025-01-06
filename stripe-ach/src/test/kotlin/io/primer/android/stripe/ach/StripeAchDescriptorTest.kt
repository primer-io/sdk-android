package io.primer.android.stripe.ach

import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.paymentmethods.VaultCapability
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class StripeAchDescriptorTest {
    @MockK
    lateinit var mockLocalConfig: PrimerConfig

    @MockK
    lateinit var mockConfig: PaymentMethodConfigDataResponse

    @InjectMockKs
    internal lateinit var stripeAchDescriptor: StripeAchDescriptor

    @Test
    fun `vaultCapability returns SINGLE_USE_ONLY`() {
        val capability = stripeAchDescriptor.vaultCapability
        assertEquals(VaultCapability.SINGLE_USE_ONLY, capability)
    }

    @Test
    fun `headlessDefinition returns definition with NATIVE_UI category`() {
        val definition = stripeAchDescriptor.headlessDefinition
        assertEquals(1, definition.paymentMethodManagerCategories.size)
        assertEquals(PrimerPaymentMethodManagerCategory.STRIPE_ACH, definition.paymentMethodManagerCategories[0])
    }
}
