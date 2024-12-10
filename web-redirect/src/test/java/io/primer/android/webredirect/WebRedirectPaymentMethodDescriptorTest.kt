package io.primer.android.webredirect

import io.mockk.mockk
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.paymentmethods.VaultCapability
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class WebRedirectPaymentMethodDescriptorTest {

    private lateinit var descriptor: WebRedirectPaymentMethodDescriptor
    private lateinit var options: WebRedirectPaymentMethod
    private lateinit var localConfig: PrimerConfig
    private lateinit var config: PaymentMethodConfigDataResponse

    @BeforeEach
    fun setUp() {
        options = mockk(relaxed = true)
        localConfig = mockk(relaxed = true)
        config = mockk(relaxed = true)
        descriptor = WebRedirectPaymentMethodDescriptor(options, localConfig, config)
    }

    @Test
    fun `vaultCapability should be SINGLE_USE_ONLY`() {
        assertEquals(VaultCapability.SINGLE_USE_ONLY, descriptor.vaultCapability)
    }

    @Test
    fun `headlessDefinition should contain NATIVE_UI category`() {
        val headlessDefinition = descriptor.headlessDefinition

        assertNotNull(headlessDefinition)
        assertEquals(1, headlessDefinition.paymentMethodManagerCategories.size)
        assertEquals(
            expected = PrimerPaymentMethodManagerCategory.NATIVE_UI,
            actual = headlessDefinition.paymentMethodManagerCategories.first()
        )
    }
}
