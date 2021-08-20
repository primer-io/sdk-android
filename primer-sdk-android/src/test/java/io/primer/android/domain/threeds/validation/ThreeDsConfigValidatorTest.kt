package io.primer.android.domain.threeds.validation

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.primer.android.UXMode
import io.primer.android.threeds.domain.models.ThreeDsConfigParams
import io.primer.android.threeds.domain.validation.ThreeDsConfigValidator
import io.primer.android.threeds.domain.validation.ThreeDsConfigValidator.Companion.AMOUNT_MISSING_ERROR
import io.primer.android.threeds.domain.validation.ThreeDsConfigValidator.Companion.CURRENCY_MISSING_ERROR
import io.primer.android.threeds.domain.validation.ThreeDsConfigValidator.Companion.ORDER_ID_MISSING_ERROR
import io.primer.android.threeds.domain.validation.ThreeDsConfigValidator.Companion.USER_DETAILS_ADDRESS_LINE_1_MISSING_ERROR
import io.primer.android.threeds.domain.validation.ThreeDsConfigValidator.Companion.USER_DETAILS_CITY_MISSING_ERROR
import io.primer.android.threeds.domain.validation.ThreeDsConfigValidator.Companion.USER_DETAILS_COUNTRY_CODE_MISSING_ERROR
import io.primer.android.threeds.domain.validation.ThreeDsConfigValidator.Companion.USER_DETAILS_EMAIL_MISSING_ERROR
import io.primer.android.threeds.domain.validation.ThreeDsConfigValidator.Companion.USER_DETAILS_FIRST_NAME_MISSING_ERROR
import io.primer.android.threeds.domain.validation.ThreeDsConfigValidator.Companion.USER_DETAILS_LAST_NAME_MISSING_ERROR
import io.primer.android.threeds.domain.validation.ThreeDsConfigValidator.Companion.USER_DETAILS_MISSING_ERROR
import io.primer.android.threeds.domain.validation.ThreeDsConfigValidator.Companion.USER_DETAILS_POSTAL_CODE_MISSING_ERROR
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
internal class ThreeDsConfigValidatorTest {

    private lateinit var validator: ThreeDsConfigValidator

    @MockK(relaxed = true)
    lateinit var configParams: ThreeDsConfigParams

    @BeforeEach
    fun setUp() {
        setupThreeDsConfigParams()
        validator = ThreeDsConfigValidator()
    }

    @Test
    fun `validate() should throw IllegalArgumentException with message AMOUNT_MISSING_ERROR when UxMode is CHECKOUT`() {
        every { configParams.uxMode }.returns(UXMode.CHECKOUT)
        every { configParams.amount }.returns(0)

        val exception = assertThrows<IllegalArgumentException> {
            runBlockingTest {
                validator.validate(configParams).first()
            }
        }

        assertEquals(validator.getFormattedMessage(listOf(AMOUNT_MISSING_ERROR)), exception.message)
    }

    @Test
    fun `validate() should throw IllegalArgumentException with message CURRENCY_MISSING_ERROR`() {
        every { configParams.currency }.returns("")

        val exception = assertThrows<IllegalArgumentException> {
            runBlockingTest {
                validator.validate(configParams).first()
            }
        }

        assertEquals(
            validator.getFormattedMessage(listOf(CURRENCY_MISSING_ERROR)),
            exception.message
        )
    }

    @Test
    fun `validate() should throw IllegalArgumentException with message CURRENCY_MISSING_ERROR when currency is invalid`() {
        every { configParams.currency }.returns(INVALID_CURRENCY)

        val exception = assertThrows<IllegalArgumentException> {
            runBlockingTest {
                validator.validate(configParams).first()
            }
        }
        assertEquals(
            validator.getFormattedMessage(listOf(CURRENCY_MISSING_ERROR)),
            exception.message
        )
    }

    @Test
    fun `validate() should throw IllegalArgumentException with message ORDER_ID_MISSING_ERROR`() {
        every { configParams.orderId }.returns("")

        val exception = assertThrows<IllegalArgumentException> {
            runBlockingTest {
                validator.validate(configParams).first()
            }
        }

        assertEquals(
            validator.getFormattedMessage(listOf(ORDER_ID_MISSING_ERROR)),
            exception.message
        )
    }

    @Test
    fun `validate() should throw IllegalArgumentException with message USER_DETAILS_MISSING_ERROR`() {
        every { configParams.userDetailsAvailable }.returns(false)

        val exception = assertThrows<IllegalArgumentException> {
            runBlockingTest {
                validator.validate(configParams).first()
            }
        }

        assertEquals(
            validator.getFormattedMessage(listOf(USER_DETAILS_MISSING_ERROR)),
            exception.message
        )
    }

    @Test
    fun `validate() should throw IllegalArgumentException with message USER_DETAILS_FIRST_NAME_MISSING_ERROR`() {
        every { configParams.customerFirstName }.returns("")

        val exception = assertThrows<IllegalArgumentException> {
            runBlockingTest {
                validator.validate(configParams).first()
            }
        }

        assertEquals(
            validator.getFormattedMessage(listOf(USER_DETAILS_FIRST_NAME_MISSING_ERROR)),
            exception.message
        )
    }

    @Test
    fun `validate() should throw IllegalArgumentException with message USER_DETAILS_LAST_NAME_MISSING_ERROR`() {
        every { configParams.customerLastName }.returns("")

        val exception = assertThrows<IllegalArgumentException> {
            runBlockingTest {
                validator.validate(configParams).first()
            }
        }

        assertEquals(
            validator.getFormattedMessage(listOf(USER_DETAILS_LAST_NAME_MISSING_ERROR)),
            exception.message
        )
    }

    @Test
    fun `validate() should throw IllegalArgumentException with message USER_DETAILS_EMAIL_MISSING_ERROR`() {
        every { configParams.customerEmail }.returns("")

        val exception = assertThrows<IllegalArgumentException> {
            runBlockingTest {
                validator.validate(configParams).first()
            }
        }

        assertEquals(
            validator.getFormattedMessage(listOf(USER_DETAILS_EMAIL_MISSING_ERROR)),
            exception.message
        )
    }

    @Test
    fun `validate() should throw IllegalArgumentException with message USER_DETAILS_CITY_MISSING_ERROR`() {
        every { configParams.city }.returns("")

        val exception = assertThrows<IllegalArgumentException> {
            runBlockingTest {
                validator.validate(configParams).first()
            }
        }

        assertEquals(
            validator.getFormattedMessage(listOf(USER_DETAILS_CITY_MISSING_ERROR)),
            exception.message
        )
    }

    @Test
    fun `validate() should throw IllegalArgumentException with message USER_DETAILS_ADDRESS_LINE_1_MISSING_ERROR`() {
        every { configParams.addressLine1 }.returns("")

        val exception = assertThrows<IllegalArgumentException> {
            runBlockingTest {
                validator.validate(configParams).first()
            }
        }

        assertEquals(
            validator.getFormattedMessage(listOf(USER_DETAILS_ADDRESS_LINE_1_MISSING_ERROR)),
            exception.message
        )
    }

    @Test
    fun `validate() should throw IllegalArgumentException with message USER_DETAILS_POSTAL_CODE_MISSING_ERROR`() {
        every { configParams.postalCode }.returns("")

        val exception = assertThrows<IllegalArgumentException> {
            runBlockingTest {
                validator.validate(configParams).first()
            }
        }

        assertEquals(
            validator.getFormattedMessage(listOf(USER_DETAILS_POSTAL_CODE_MISSING_ERROR)),
            exception.message
        )
    }

    @Test
    fun `validate() should throw IllegalArgumentException with message USER_DETAILS_COUNTRY_CODE_MISSING_ERROR`() {
        every { configParams.countryCode }.returns("")

        val exception = assertThrows<IllegalArgumentException> {
            runBlockingTest {
                validator.validate(configParams).first()
            }
        }

        assertEquals(
            validator.getFormattedMessage(listOf(USER_DETAILS_COUNTRY_CODE_MISSING_ERROR)),
            exception.message
        )
    }

    @Test
    fun `validate() should throw IllegalArgumentException with error messages joined when inputs are missing and when UxMode is CHECKOUT`() {
        every { configParams.uxMode }.returns(UXMode.CHECKOUT)
        every { configParams.amount }.returns(0)
        every { configParams.currency }.returns("")
        every { configParams.orderId }.returns("")
        every { configParams.userDetailsAvailable }.returns(false)
        every { configParams.customerFirstName }.returns("")
        every { configParams.customerLastName }.returns("")
        every { configParams.customerEmail }.returns("")
        every { configParams.city }.returns("")
        every { configParams.addressLine1 }.returns("")
        every { configParams.postalCode }.returns("")
        every { configParams.countryCode }.returns("")

        val exception = assertThrows<IllegalArgumentException> {
            runBlockingTest {
                validator.validate(configParams).first()
            }
        }

        assertEquals(
            validator.getFormattedMessage(
                listOf(
                    AMOUNT_MISSING_ERROR,
                    CURRENCY_MISSING_ERROR,
                    ORDER_ID_MISSING_ERROR,
                    USER_DETAILS_MISSING_ERROR,
                    USER_DETAILS_FIRST_NAME_MISSING_ERROR,
                    USER_DETAILS_LAST_NAME_MISSING_ERROR,
                    USER_DETAILS_EMAIL_MISSING_ERROR,
                    USER_DETAILS_CITY_MISSING_ERROR,
                    USER_DETAILS_ADDRESS_LINE_1_MISSING_ERROR,
                    USER_DETAILS_POSTAL_CODE_MISSING_ERROR,
                    USER_DETAILS_COUNTRY_CODE_MISSING_ERROR
                )
            ),
            exception.message
        )
    }

    @Test
    fun `validate() should emit Unit when ThreeDsConfigParams are validated`() {
        runBlockingTest {
            val result = validator.validate(configParams).first()
            assertEquals(Unit, result)
        }
    }

    private fun setupThreeDsConfigParams() {
        every { configParams.amount }.returns(DEFAULT_AMOUNT)
        every { configParams.currency }.returns(DEFAULT_CURRENCY)
        every { configParams.orderId }.returns(DEFAULT_ORDER_ID)
        every { configParams.userDetailsAvailable }.returns(true)
        every { configParams.customerFirstName }.returns(DEFAULT_FIRST_NAME)
        every { configParams.customerLastName }.returns(DEFAULT_LAST_NAME)
        every { configParams.customerEmail }.returns(DEFAULT_EMAIL)
        every { configParams.city }.returns(DEFAULT_CITY)
        every { configParams.addressLine1 }.returns(DEFAULT_ADDRESS_1)
        every { configParams.postalCode }.returns(DEFAULT_POSTAL_CODE)
        every { configParams.countryCode }.returns(DEFAULT_COUNTRY_CODE)
    }

    private companion object {

        const val DEFAULT_AMOUNT = 10
        const val INVALID_CURRENCY = "EU"
        const val DEFAULT_CURRENCY = "EUR"
        const val DEFAULT_ORDER_ID = "123"
        const val DEFAULT_FIRST_NAME = "first name"
        const val DEFAULT_LAST_NAME = "last name"
        const val DEFAULT_EMAIL = "email@primer.io"
        const val DEFAULT_CITY = "city"
        const val DEFAULT_ADDRESS_1 = "address 1"
        const val DEFAULT_POSTAL_CODE = "34353"
        const val DEFAULT_COUNTRY_CODE = "DE"
    }
}
