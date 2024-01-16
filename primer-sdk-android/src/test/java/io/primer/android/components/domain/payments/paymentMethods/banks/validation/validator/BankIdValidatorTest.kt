package io.primer.android.components.domain.payments.paymentMethods.banks.validation.validator

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.domain.rpc.banks.models.IssuingBank
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
internal class BankIdValidatorTest {

    @Test
    fun `validate() should return PrimerValidationError when 'banks' is null`() {
        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns "uuid"
        val result = BankIdValidator.validate(banks = null, bankId = "bankId")

        assertEquals(
            PrimerValidationError(
                errorId = BanksValidations.BANKS_NOT_LOADED_ERROR_ID,
                description = "Banks need to be loaded before bank id can be collected.",
                diagnosticsId = "uuid"
            ),
            result
        )
        unmockkStatic(UUID::class)
    }

    @Test
    fun `validate() should return PrimerValidationError when there's no matching bank for the bank id`() {
        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns "uuid"
        val banks = listOf(mockk<IssuingBank>() { every { id } returns "otherBankId" })
        val result = BankIdValidator.validate(banks = banks, bankId = "bankId")

        assertEquals(
            PrimerValidationError(
                errorId = BanksValidations.INVALID_BANK_ID_ERROR_ID,
                description = "Bank id doesn't belong to any of the supported banks.",
                diagnosticsId = "uuid"
            ),
            result
        )
        unmockkStatic(UUID::class)
    }

    @Test
    fun `validate() should return null when there's a matching bank for the bank id`() {
        val banks = listOf(mockk<IssuingBank>() { every { id } returns "bankId" })
        val result = BankIdValidator.validate(banks = banks, bankId = "bankId")

        assertNull(result)
    }
}
