package io.primer.android.components.implementation.core.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.components.SdkUninitializedException
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.domain.exception.UnsupportedPaymentMethodManagerException
import io.primer.android.components.validation.resolvers.PaymentMethodManagerInitValidationRulesResolver
import io.primer.android.components.validation.rules.PaymentMethodManagerInitValidationData
import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.core.domain.validation.ValidationRule
import io.primer.android.domain.exception.UnsupportedPaymentMethodException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@ExperimentalCoroutinesApi
class DefaultPaymentMethodInitializerTest {
    private lateinit var initValidationRulesResolver: PaymentMethodManagerInitValidationRulesResolver
    private lateinit var analyticsInteractor: AnalyticsInteractor
    private lateinit var paymentMethodInitializer: DefaultPaymentMethodInitializer

    @BeforeEach
    fun setUp() {
        initValidationRulesResolver = mockk()
        analyticsInteractor = mockk(relaxed = true)
        paymentMethodInitializer = DefaultPaymentMethodInitializer(initValidationRulesResolver, analyticsInteractor)
    }

    @Test
    fun `init should add analytics event`() =
        runTest {
            val paymentMethodType = "testType"
            val category = PrimerPaymentMethodManagerCategory.NATIVE_UI

            coEvery { initValidationRulesResolver.resolve().rules } returns listOf()

            paymentMethodInitializer.init(paymentMethodType, category)

            coVerify {
                analyticsInteractor(
                    SdkFunctionParams(
                        "newInstance",
                        mapOf("paymentMethodType" to paymentMethodType, "category" to category.name),
                    ),
                )
            }
        }

    @Test
    fun `init should validate using validation rules and throw exceptions on failures`() {
        val paymentMethodType = "testType"
        val category = PrimerPaymentMethodManagerCategory.NATIVE_UI

        val failureResult = ValidationResult.Failure(SdkUninitializedException())

        val validationRule =
            mockk<ValidationRule<PaymentMethodManagerInitValidationData>> {
                every { validate(any()) } returns failureResult
            }

        coEvery { initValidationRulesResolver.resolve().rules } returns listOf(validationRule)

        assertThrows<SdkUninitializedException> {
            runTest {
                paymentMethodInitializer.init(paymentMethodType, category)
            }
        }
    }

    @Test
    fun `init should throw UnsupportedPaymentMethodManagerException on validation failure`() {
        val paymentMethodType = "testType"
        val category = PrimerPaymentMethodManagerCategory.NATIVE_UI

        val failureResult =
            ValidationResult.Failure(
                UnsupportedPaymentMethodManagerException(
                    paymentMethodType = paymentMethodType,
                    category = PrimerPaymentMethodManagerCategory.NATIVE_UI,
                ),
            )

        val validationRule =
            mockk<ValidationRule<PaymentMethodManagerInitValidationData>> {
                every { validate(any()) } returns failureResult
            }

        coEvery { initValidationRulesResolver.resolve().rules } returns listOf(validationRule)

        val exception =
            assertThrows<UnsupportedPaymentMethodManagerException> {
                runTest {
                    paymentMethodInitializer.init(paymentMethodType, category)
                }
            }

        assert(exception.message == "Payment method $paymentMethodType is not supported on $category manager")
    }

    @Test
    fun `init should throw UnsupportedPaymentMethodException on validation failure`() {
        val paymentMethodType = "testType"
        val category = PrimerPaymentMethodManagerCategory.NATIVE_UI

        val failureResult = ValidationResult.Failure(UnsupportedPaymentMethodException(paymentMethodType))

        val validationRule =
            mockk<ValidationRule<PaymentMethodManagerInitValidationData>> {
                every { validate(any()) } returns failureResult
            }

        coEvery { initValidationRulesResolver.resolve().rules } returns listOf(validationRule)

        val exception =
            assertThrows<UnsupportedPaymentMethodException> {
                runTest {
                    paymentMethodInitializer.init(paymentMethodType, category)
                }
            }

        assert(exception.message == "Cannot present $paymentMethodType because it is not supported.")
    }
}
