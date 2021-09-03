package io.primer.android.domain.payments.apaya.validation

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.primer.android.domain.payments.apaya.models.ApayaWebResultParams
import io.primer.android.domain.payments.apaya.validation.ApayaWebResultValidator.Companion.INVALID_WEB_VIEW_RESULT
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
internal class ApayaWebResultValidatorTest {

    private lateinit var validator: ApayaWebResultValidator

    @MockK(relaxed = true)
    lateinit var params: ApayaWebResultParams

    @BeforeEach
    fun setUp() {
        setupApayaWebResultParams()
        validator = ApayaWebResultValidator()
    }

    @Test
    fun `validate() should throw IllegalArgumentException with message INVALID_WEB_VIEW_RESULT when mcc is empty`() {
        every { params.mcc }.returns("")
        val exception = assertThrows<IllegalArgumentException> {
            runBlockingTest {
                validator.validate(params).first()
            }
        }

        Assertions.assertEquals(INVALID_WEB_VIEW_RESULT, exception.message)
    }

    @Test
    fun `validate() should throw IllegalArgumentException with message INVALID_WEB_VIEW_RESULT when mnc is empty`() {
        every { params.mnc }.returns("")
        val exception = assertThrows<IllegalArgumentException> {
            runBlockingTest {
                validator.validate(params).first()
            }
        }

        Assertions.assertEquals(INVALID_WEB_VIEW_RESULT, exception.message)
    }

    @Test
    fun `validate() should throw IllegalArgumentException with message INVALID_WEB_VIEW_RESULT when mxNumber is empty`() {
        every { params.mxNumber }.returns("")
        val exception = assertThrows<IllegalArgumentException> {
            runBlockingTest {
                validator.validate(params).first()
            }
        }

        Assertions.assertEquals(INVALID_WEB_VIEW_RESULT, exception.message)
    }

    @Test
    fun `validate() should throw IllegalArgumentException with message INVALID_WEB_VIEW_RESULT when hashedIdentifier is empty`() {
        every { params.hashedIdentifier }.returns("")
        val exception = assertThrows<IllegalArgumentException> {
            runBlockingTest {
                validator.validate(params).first()
            }
        }

        Assertions.assertEquals(INVALID_WEB_VIEW_RESULT, exception.message)
    }

    @Test
    fun `validate() should throw IllegalArgumentException with message INVALID_WEB_VIEW_RESULT when success is invalid`() {
        every { params.success }.returns(INVALID_SUCCESS)
        val exception = assertThrows<IllegalArgumentException> {
            runBlockingTest {
                validator.validate(params).first()
            }
        }

        Assertions.assertEquals(INVALID_WEB_VIEW_RESULT, exception.message)
    }

    @Test
    fun `validate() should emit Unit when ApayaWebResultParams are validated`() {
        runBlockingTest {
            val result = validator.validate(params).first()
            Assertions.assertEquals(Unit, result)
        }
    }

    private fun setupApayaWebResultParams() {
        every { params.mcc }.returns(DEFAULT_MCC)
        every { params.mnc }.returns(DEFAULT_MNC)
        every { params.mxNumber }.returns(DEFAULT_MX_NUMBER)
        every { params.hashedIdentifier }.returns(DEFAULT_HASHED_IDENTIFIER)
        every { params.success }.returns(VALID_SUCCESS)
    }

    private companion object {

        const val DEFAULT_MCC = "mcc"
        const val DEFAULT_MNC = "mnc"
        const val DEFAULT_MX_NUMBER = "mxNumber"
        const val DEFAULT_HASHED_IDENTIFIER = "hashed_id"
        const val INVALID_SUCCESS = "0"
        const val VALID_SUCCESS = "1"
    }
}
