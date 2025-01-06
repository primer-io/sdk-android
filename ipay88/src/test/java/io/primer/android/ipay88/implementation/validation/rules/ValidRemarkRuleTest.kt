package io.primer.android.ipay88.implementation.validation.rules

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.errors.data.exception.IllegalClientSessionValueException
import io.primer.android.ipay88.InstantExecutorExtension
import io.primer.android.ipay88.implementation.errors.data.exception.IPay88IllegalValueKey
import io.primer.android.ipay88.implementation.validation.IPay88ValidationData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
internal class ValidRemarkRuleTest {
    private lateinit var remarkRule: ValidRemarkRule

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        remarkRule = ValidRemarkRule()
    }

    @Test
    fun `validate() should return Success result when customer id validation is successful`() {
        val iPay88ValidationData = mockk<IPay88ValidationData>(relaxed = true)
        every { iPay88ValidationData.clientSession?.clientSession?.customerId }.returns(
            "1234",
        )

        val validationResult = remarkRule.validate(iPay88ValidationData)
        assertEquals(ValidationResult.Success, validationResult)
    }

    @Test
    fun `validate() should return Success result when customer id is null and actionType is empty`() {
        val iPay88ValidationData = mockk<IPay88ValidationData>(relaxed = true)
        every { iPay88ValidationData.clientSession?.clientSession?.customerId }.returns(
            null,
        )
        every { iPay88ValidationData.clientToken.actionType }.returns("")
        val validationResult = remarkRule.validate(iPay88ValidationData)
        assertEquals(ValidationResult.Success, validationResult)
    }

    @Test
    fun `validate() should return Failure result when customer id validation failed and actionType is not empty`() {
        val iPay88ValidationData = mockk<IPay88ValidationData>(relaxed = true)
        every { iPay88ValidationData.clientSession?.clientSession?.customerId }.returns(
            null,
        )
        every { iPay88ValidationData.clientToken.actionType }.returns("BT")
        val validationResult = remarkRule.validate(iPay88ValidationData)
        val exception =
            (validationResult as ValidationResult.Failure).exception
                as IllegalClientSessionValueException
        assertEquals(
            IllegalClientSessionValueException::class,
            exception::class,
        )
        assertEquals(
            IPay88IllegalValueKey.ILLEGAL_CUSTOMER_ID,
            exception.key,
        )
    }
}
