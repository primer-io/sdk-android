package io.primer.android.ipay88.implementation.validation.rules

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.domain.action.models.PrimerLineItem
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
internal class ValidProductDescriptionRuleTest {
    private lateinit var productDescriptionRule: ValidProductDescriptionRule

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        productDescriptionRule = ValidProductDescriptionRule()
    }

    @Test
    fun `validate() should return Success result when productDescription validation is successful`() {
        val iPay88ValidationData = mockk<IPay88ValidationData>(relaxed = true)
        val lineItem = mockk<PrimerLineItem>(relaxed = true)
        every { lineItem.itemDescription }.returns("Test")
        every { iPay88ValidationData.clientSession?.clientSession?.lineItems }.returns(
            listOf(lineItem),
        )

        val validationResult = productDescriptionRule.validate(iPay88ValidationData)
        assertEquals(ValidationResult.Success, validationResult)
    }

    @Test
    fun `validate() should return Failure result when customer productDescription validation failed`() {
        val iPay88ValidationData = mockk<IPay88ValidationData>(relaxed = true)
        every { iPay88ValidationData.clientSession?.clientSession?.lineItems }.returns(
            emptyList(),
        )
        val validationResult = productDescriptionRule.validate(iPay88ValidationData)
        val exception =
            (validationResult as ValidationResult.Failure).exception
                as IllegalClientSessionValueException
        assertEquals(
            IllegalClientSessionValueException::class,
            exception::class,
        )
        assertEquals(
            IPay88IllegalValueKey.ILLEGAL_PRODUCT_DESCRIPTION,
            exception.key,
        )
    }
}
