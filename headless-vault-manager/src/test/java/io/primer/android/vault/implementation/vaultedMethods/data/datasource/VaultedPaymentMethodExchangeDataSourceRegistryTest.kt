package io.primer.android.vault.implementation.vaultedMethods.data.datasource

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.primer.android.components.domain.payments.vault.model.card.PrimerVaultedCardAdditionalData
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.vault.implementation.vaultedMethods.domain.PrimerVaultedPaymentMethodAdditionalData
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class VaultedPaymentMethodExchangeDataSourceRegistryTest {
    @RelaxedMockK
    internal lateinit var httpClient: PrimerHttpClient

    private lateinit var registry: VaultedPaymentMethodExchangeDataSourceRegistry

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        registry = VaultedPaymentMethodExchangeDataSourceRegistry(httpClient) { PrimerApiVersion.LATEST }
    }

    @Test
    fun `getExchangeDataSource() should return RemoteEmptyExchangeDataSource when additional data is null`() {
        runTest {
            val validator = registry.getExchangeDataSource(null)
            assertEquals(RemoteEmptyExchangeDataSource::class, validator::class)
        }
    }

    @Test
    fun `getExchangeDataSource() should return RemoteVaultedCardExchangeDataSource when additional data is PrimerVaultedCardAdditionalData`() {
        val additionalData = mockk<PrimerVaultedCardAdditionalData>(relaxed = true)
        runTest {
            val validator = registry.getExchangeDataSource(additionalData)
            assertEquals(RemoteVaultedCardExchangeDataSource::class, validator::class)
        }
    }

    @Test
    fun `getExchangeDataSource() should throw IllegalArgumentException when exchange datasource for additional data is not registered`() {
        val additionalData = mockk<PrimerVaultedPaymentMethodAdditionalData>(relaxed = true)
        assertThrows<IllegalArgumentException> {
            runTest {
                registry.getExchangeDataSource(additionalData)
            }
        }
    }
}
