package io.primer.android.card

import io.mockk.mockk
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.paymentmethods.HeadlessDefinition
import io.primer.android.paymentmethods.VaultCapability
import io.primer.android.components.domain.core.models.card.PrimerCardData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CreditCardTest {
    @Test
    fun `vaultCapability should return SINGLE_USE_AND_VAULT`() {
        val localConfig = mockk<PrimerConfig>()
        val config = mockk<PaymentMethodConfigDataResponse>()

        val creditCard = CreditCard(localConfig, config)

        assertEquals(VaultCapability.SINGLE_USE_AND_VAULT, creditCard.vaultCapability)
    }

    @Test
    fun `headlessDefinition should return correct HeadlessDefinition`() {
        val localConfig = mockk<PrimerConfig>()
        val config = mockk<PaymentMethodConfigDataResponse>()

        val creditCard = CreditCard(localConfig, config)
        val expectedHeadlessDefinition = HeadlessDefinition(
            listOf(PrimerPaymentMethodManagerCategory.RAW_DATA),
            rawDataDefinition = HeadlessDefinition.RawDataDefinition(PrimerCardData::class)
        )

        assertEquals(expectedHeadlessDefinition, creditCard.headlessDefinition)
    }
}
