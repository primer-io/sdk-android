package io.primer.android.klarna

import io.mockk.mockk
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.paymentmethods.HeadlessDefinition
import io.primer.android.paymentmethods.VaultCapability
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class KlarnaDescriptorTest {
    private lateinit var klarna: Klarna
    private lateinit var localConfig: PrimerConfig
    private lateinit var config: PaymentMethodConfigDataResponse
    private lateinit var descriptor: KlarnaDescriptor

    @BeforeEach
    fun setUp() {
        klarna = mockk()
        localConfig = mockk(relaxed = true)
        config = mockk()
        descriptor = KlarnaDescriptor(klarna, localConfig, config)
    }

    @Test
    fun `vaultCapability should be SINGLE_USE_AND_VAULT`() {
        assertEquals(VaultCapability.SINGLE_USE_AND_VAULT, descriptor.vaultCapability)
    }

    @Test
    fun `headlessDefinition should return HeadlessDefinition with NATIVE_UI category`() {
        val expectedHeadlessDefinition = HeadlessDefinition(listOf(PrimerPaymentMethodManagerCategory.KLARNA))
        assertEquals(expectedHeadlessDefinition, descriptor.headlessDefinition)
    }
}
