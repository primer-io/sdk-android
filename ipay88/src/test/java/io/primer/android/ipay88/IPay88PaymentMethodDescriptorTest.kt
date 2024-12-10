package io.primer.android.ipay88

import com.ipay.IPayIH
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.paymentmethods.VaultCapability
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class IPay88PaymentMethodDescriptorTest {

    @MockK
    private lateinit var options: IPay88PaymentMethod

    @MockK
    private lateinit var localConfig: PrimerConfig

    @MockK
    private lateinit var config: PaymentMethodConfigDataResponse

    private lateinit var descriptor: IPay88PaymentMethodDescriptor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        descriptor = IPay88PaymentMethodDescriptor(options, localConfig, config)
    }

    @Test
    fun `test paymentMethod is initialized correctly`() {
        assertEquals(IPayIH.PAY_METHOD_CREDIT_CARD, descriptor.paymentMethod)
    }

    @Test
    fun `test vaultCapability is SINGLE_USE_ONLY`() {
        assertEquals(VaultCapability.SINGLE_USE_ONLY, descriptor.vaultCapability)
    }

    @Test
    fun `test headlessDefinition includes NATIVE_UI category`() {
        val headlessDefinition = descriptor.headlessDefinition
        assertEquals(1, headlessDefinition.paymentMethodManagerCategories.size)
        assertEquals(PrimerPaymentMethodManagerCategory.NATIVE_UI, headlessDefinition.paymentMethodManagerCategories[0])
    }
}
