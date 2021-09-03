package io.primer.android.domain.payments.apaya.validation

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.primer.android.domain.payments.apaya.models.ApayaSessionParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Locale

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
internal class ApayaSessionParamsValidatorTest {

    private lateinit var validator: ApayaSessionParamsValidator

    @MockK(relaxed = true)
    lateinit var params: ApayaSessionParams

    @BeforeEach
    fun setUp() {
        setupApayaSessionParams()
        validator = ApayaSessionParamsValidator()
    }

    @Test
    fun `validate() should throw IllegalArgumentException with message INVALID_SESSION_PARAMS when merchantAccountId is empty`() {
        every { params.merchantAccountId }.returns("")
        val exception = assertThrows<IllegalArgumentException> {
            runBlockingTest {
                validator.validate(params).first()
            }
        }

        Assertions.assertEquals(
            ApayaSessionParamsValidator.INVALID_SESSION_PARAMS,
            exception.message
        )
    }

    @Test
    fun `validate() should throw IllegalArgumentException with message INVALID_SESSION_PARAMS when language is empty`() {
        every { params.locale }.returns(Locale(""))
        val exception = assertThrows<IllegalArgumentException> {
            runBlockingTest {
                validator.validate(params).first()
            }
        }

        Assertions.assertEquals(
            ApayaSessionParamsValidator.INVALID_SESSION_PARAMS,
            exception.message
        )
    }

    @Test
    fun `validate() should throw IllegalArgumentException with message INVALID_SESSION_PARAMS when currencyCode is empty`() {
        every { params.currencyCode }.returns("")
        assertThrows<IllegalArgumentException> {
            runBlockingTest {
                validator.validate(params).first()
            }
        }
    }

    @Test
    fun `validate() should throw IllegalArgumentException with message INVALID_SESSION_PARAMS when currencyCode is invalid`() {
        every { params.currencyCode }.returns(INVALID_CURRENCY)
        assertThrows<IllegalArgumentException> {
            runBlockingTest {
                validator.validate(params).first()
            }
        }
    }

    @Test
    fun `validate() should emit Unit when ApayaSessionParams are validated`() {
        runBlockingTest {
            val result = validator.validate(params).first()
            Assertions.assertEquals(Unit, result)
        }
    }

    private fun setupApayaSessionParams() {
        every { params.locale }.returns(Locale.getDefault())
        every { params.merchantAccountId }.returns(DEFAULT_MERCHANT_ACCOUNT_ID)
        every { params.currencyCode }.returns(DEFAULT_CURRENCY)
    }

    private companion object {

        const val INVALID_CURRENCY = "EU"
        const val DEFAULT_CURRENCY = "EUR"
        const val DEFAULT_MERCHANT_ACCOUNT_ID = "merchantAccountId"
    }
}
