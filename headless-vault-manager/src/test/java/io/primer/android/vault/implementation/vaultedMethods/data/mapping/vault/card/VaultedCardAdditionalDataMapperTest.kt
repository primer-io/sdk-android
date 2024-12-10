package io.primer.android.vault.implementation.vaultedMethods.data.mapping.vault.card

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import io.primer.android.vault.implementation.vaultedMethods.data.mapping.card.VaultedCardAdditionalDataMapper
import io.primer.android.vault.implementation.vaultedMethods.data.model.card.CardVaultExchangeDataRequest
import io.primer.android.components.domain.payments.vault.model.card.PrimerVaultedCardAdditionalData
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class VaultedCardAdditionalDataMapperTest {

    private lateinit var mapper: VaultedCardAdditionalDataMapper

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        mapper = VaultedCardAdditionalDataMapper()
    }

    @Test
    fun `map() should return EmptyExchangeDataRequest when additional data is null`() {
        val cvv = "123"
        val additionalData = mockk<PrimerVaultedCardAdditionalData>(relaxed = true)
        every { additionalData.cvv }.returns(cvv)

        runTest {
            val exchangeDataRequest = mapper.map(additionalData)
            assertEquals(CardVaultExchangeDataRequest::class, exchangeDataRequest::class)
            assertEquals(cvv, exchangeDataRequest.cvv)
        }
    }
}
