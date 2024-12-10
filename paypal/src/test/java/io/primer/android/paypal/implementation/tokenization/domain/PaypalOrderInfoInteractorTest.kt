package io.primer.android.paypal.implementation.tokenization.domain

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.core.domain.validation.ValidationRule
import io.primer.android.core.domain.validation.ValidationRulesChain
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalOrderInfo
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalOrderInfoParams
import io.primer.android.paypal.implementation.tokenization.domain.repository.PaypalInfoRepository
import io.primer.android.paypal.implementation.validation.resolvers.PaypalCheckoutOrderInfoValidationRulesResolver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class PaypalOrderInfoInteractorTest {

    private lateinit var interactor: PaypalOrderInfoInteractor
    private val paypalInfoRepository: PaypalInfoRepository = mockk()
    private val validationRulesResolver: PaypalCheckoutOrderInfoValidationRulesResolver = mockk()

    @BeforeEach
    fun setUp() {
        interactor = PaypalOrderInfoInteractor(
            paypalInfoRepository,
            validationRulesResolver
        )
    }

    @Test
    fun `performAction() should call getPaypalOrderInfo when validation is successful`() = runTest {
        // Given
        val params = mockk<PaypalOrderInfoParams> {
            every { orderId } returns "orderId"
        }

        val validationRule: ValidationRule<String?> = mockk {
            every { validate(any()) } returns ValidationResult.Success
        }

        val validationRulesChain = mockk<ValidationRulesChain<String?>>(relaxed = true) {
            every { rules } returns listOf(validationRule)
        }

        every { validationRulesResolver.resolve() } returns validationRulesChain

        val expectedResult = mockk<PaypalOrderInfo>(relaxed = true)
        coEvery {
            paypalInfoRepository.getPaypalOrderInfo(params)
        } returns Result.success(expectedResult)

        // When
        val result = interactor(params)

        // Then
        assertEquals(Result.success(expectedResult), result)
    }

    @Test
    fun `performAction() should result in exception when validation fails`() = runTest {
        // Given
        val errorMessage = "Invalid token"
        val params = mockk<PaypalOrderInfoParams> {
            every { orderId } returns "orderId"
        }

        val validationRule: ValidationRule<String?> = mockk {
            every { validate(any()) } returns ValidationResult.Failure(Exception(errorMessage))
        }

        val validationRulesChain = mockk<ValidationRulesChain<String?>>(relaxed = true) {
            every { rules } returns listOf(validationRule)
        }

        every { validationRulesResolver.resolve() } returns validationRulesChain

        // When
        val result = interactor(params)

        // Then
        assert(result.isFailure)
        assertEquals(errorMessage, (result.exceptionOrNull()?.message ?: ""))
    }
}
