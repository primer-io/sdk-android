package io.primer.android.components.domain.payments.vault.validation.additionalData.card

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import io.primer.android.components.domain.payments.vault.model.card.PrimerVaultedCardAdditionalData
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.ui.CardNetwork
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class VaultedCardAdditionalDataValidatorTest {

    private lateinit var validator: VaultedCardAdditionalDataValidator

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        validator = VaultedCardAdditionalDataValidator()
    }

    @Test
    fun `validate() should return list of validation errors when PrimerVaultedPaymentMethodData does contains paymentInstrument binData value`() {
        val additionalData = mockk<PrimerVaultedCardAdditionalData>(relaxed = true)
        val vaultedTokenData = mockk<PrimerVaultedPaymentMethod>(relaxed = true)

        every { vaultedTokenData.paymentInstrumentData.binData }.returns(null)

        runTest {
            val errors = validator.validate(additionalData, vaultedTokenData)
            assertEquals(1, errors.size)
            assertEquals(
                VaultedCardAdditionalDataValidator.INVALID_CVV_ERROR_ID,
                errors.first().errorId
            )
        }
    }

    @Test
    fun `validate() should return list of validation errors when PrimerVaultedCardAdditionalData does contains CVV value`() {
        val additionalData = mockk<PrimerVaultedCardAdditionalData>(relaxed = true)
        val vaultedTokenData = mockk<PrimerVaultedPaymentMethod>(relaxed = true)

        every { vaultedTokenData.paymentInstrumentData.binData?.network }.returns(
            CardNetwork.Type.VISA.name
        )

        runTest {
            val errors = validator.validate(additionalData, vaultedTokenData)
            assertEquals(1, errors.size)
            assertEquals(
                VaultedCardAdditionalDataValidator.INVALID_CVV_ERROR_ID,
                errors.first().errorId
            )
        }
    }

    @Test
    fun `validate() should return list of validation errors when PrimerVaultedCardAdditionalData contains invalid length CVV value`() {
        val additionalData = mockk<PrimerVaultedCardAdditionalData>(relaxed = true)
        val vaultedTokenData = mockk<PrimerVaultedPaymentMethod>(relaxed = true)

        every { additionalData.cvv }.returns("123")

        every { vaultedTokenData.paymentInstrumentData.binData?.network }.returns(
            CardNetwork.Type.AMEX.name
        )

        runTest {
            val errors = validator.validate(additionalData, vaultedTokenData)
            assertEquals(1, errors.size)
            assertEquals(
                VaultedCardAdditionalDataValidator.INVALID_CVV_ERROR_ID,
                errors.first().errorId
            )
        }
    }

    @Test
    fun `validate() should return list of validation errors when PrimerVaultedCardAdditionalData contains invalid CVV charachters`() {
        val additionalData = mockk<PrimerVaultedCardAdditionalData>(relaxed = true)
        val vaultedTokenData = mockk<PrimerVaultedPaymentMethod>(relaxed = true)

        every { additionalData.cvv }.returns("123a")

        every { vaultedTokenData.paymentInstrumentData.binData?.network }.returns(
            CardNetwork.Type.AMEX.name
        )

        runTest {
            val errors = validator.validate(additionalData, vaultedTokenData)
            assertEquals(1, errors.size)
            assertEquals(
                VaultedCardAdditionalDataValidator.INVALID_CVV_ERROR_ID,
                errors.first().errorId
            )
        }
    }

    @Test
    fun `validate() should return empty list of validation errors when PrimerVaultedCardAdditionalData contains valid length CVV value`() {
        val additionalData = mockk<PrimerVaultedCardAdditionalData>(relaxed = true)
        val vaultedTokenData = mockk<PrimerVaultedPaymentMethod>(relaxed = true)

        every { additionalData.cvv }.returns("1234")

        every { vaultedTokenData.paymentInstrumentData.binData?.network }.returns(
            CardNetwork.Type.AMEX.name
        )

        runTest {
            val errors = validator.validate(additionalData, vaultedTokenData)
            assertTrue(errors.isEmpty())
        }
    }
}
