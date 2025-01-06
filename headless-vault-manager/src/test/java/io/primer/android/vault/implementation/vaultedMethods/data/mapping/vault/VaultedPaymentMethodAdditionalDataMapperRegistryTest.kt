package io.primer.android.vault.implementation.vaultedMethods.data.mapping.vault

import io.mockk.MockKAnnotations
import io.mockk.mockk
import io.primer.android.components.domain.payments.vault.model.card.PrimerVaultedCardAdditionalData
import io.primer.android.vault.implementation.vaultedMethods.data.mapping.VaultedPaymentMethodAdditionalDataMapperRegistry
import io.primer.android.vault.implementation.vaultedMethods.data.mapping.card.VaultedCardAdditionalDataMapper
import io.primer.android.vault.implementation.vaultedMethods.data.mapping.default.EmptyAdditionalDataMapper
import io.primer.android.vault.implementation.vaultedMethods.domain.PrimerVaultedPaymentMethodAdditionalData
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class VaultedPaymentMethodAdditionalDataMapperRegistryTest {
    private lateinit var registry: VaultedPaymentMethodAdditionalDataMapperRegistry

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        registry = VaultedPaymentMethodAdditionalDataMapperRegistry()
    }

    @Test
    fun `getMapper() should return EmptyAdditionalDataMapper when additional data is null`() {
        runTest {
            val validator = registry.getMapper(null)
            assertEquals(EmptyAdditionalDataMapper::class, validator::class)
        }
    }

    @Test
    fun `getMapper() should return VaultedCardAdditionalDataMapper when additional data is PrimerVaultedCardAdditionalData`() {
        val additionalData = mockk<PrimerVaultedCardAdditionalData>(relaxed = true)
        runTest {
            val validator = registry.getMapper(additionalData)
            assertEquals(VaultedCardAdditionalDataMapper::class, validator::class)
        }
    }

    @Test
    fun `getMapper() should throw IllegalArgumentException when mapper for additional data is not registered`() {
        val additionalData = mockk<PrimerVaultedPaymentMethodAdditionalData>(relaxed = true)
        assertThrows<IllegalArgumentException> {
            runTest {
                registry.getMapper(additionalData)
            }
        }
    }
}
