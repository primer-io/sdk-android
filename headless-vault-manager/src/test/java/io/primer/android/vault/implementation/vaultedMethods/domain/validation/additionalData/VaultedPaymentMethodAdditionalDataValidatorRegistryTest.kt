package io.primer.android.vault.implementation.vaultedMethods.domain.validation.additionalData

import io.mockk.MockKAnnotations
import io.mockk.mockk
import io.primer.android.vault.implementation.vaultedMethods.domain.PrimerVaultedPaymentMethodAdditionalData
import io.primer.android.components.domain.payments.vault.model.card.PrimerVaultedCardAdditionalData
import io.primer.android.vault.implementation.vaultedMethods.domain.validation.additionalData.card.VaultedCardAdditionalDataValidator
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class VaultedPaymentMethodAdditionalDataValidatorRegistryTest {

    private lateinit var registry: VaultedPaymentMethodAdditionalDataValidatorRegistry

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        registry = VaultedPaymentMethodAdditionalDataValidatorRegistry()
    }

    @Test
    fun `getValidator() should return VaultedCardAdditionalDataValidator when additional data is PrimerVaultedCardAdditionalData`() {
        val additionalData = mockk<PrimerVaultedCardAdditionalData>(relaxed = true)
        runTest {
            val validator = registry.getValidator(additionalData)
            assertEquals(VaultedCardAdditionalDataValidator::class, validator::class)
        }
    }

    @Test
    fun `getValidator() should throw IllegalArgumentException when validator for additional data is not registered`() {
        val additionalData = mockk<PrimerVaultedPaymentMethodAdditionalData>(relaxed = true)
        assertThrows<IllegalArgumentException> {
            runTest {
                registry.getValidator(additionalData)
            }
        }
    }
}
