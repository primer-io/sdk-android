package io.primer.composable.internal.data.mappers

import io.mockk.mockk
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.configuration.domain.model.Surcharge
import io.primer.android.internal.data.mappers.PaymentMethodMapperImpl
import io.primer.android.internal.domain.models.PrimerComposablePaymentMethod
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PaymentMethodMapperImplTest {

    private lateinit var mapper: PaymentMethodMapperImpl

    @BeforeEach
    fun setUp() {
        mapper = PaymentMethodMapperImpl()
    }

    private fun createHeadlessPaymentMethod(
        paymentMethodType: String = "PAYMENT_CARD",
        paymentMethodName: String? = "Credit Card",
        supportedIntents: List<PrimerSessionIntent> = listOf(PrimerSessionIntent.CHECKOUT),
        categories: List<PrimerPaymentMethodManagerCategory> = listOf(PrimerPaymentMethodManagerCategory.NATIVE_UI),
    ) = PrimerHeadlessUniversalCheckoutPaymentMethod(
        paymentMethodType = paymentMethodType,
        paymentMethodName = paymentMethodName,
        supportedPrimerSessionIntents = supportedIntents,
        paymentMethodManagerCategories = categories,
    )

    private fun assertPaymentMethodEquals(
        expected: PrimerHeadlessUniversalCheckoutPaymentMethod,
        actual: PrimerComposablePaymentMethod,
        expectedSurcharge: Surcharge? = null,
    ) {
        assertEquals(expected.paymentMethodType, actual.paymentMethodType)
        assertEquals(expected.paymentMethodName, actual.paymentMethodName)
        assertEquals(expected.supportedPrimerSessionIntents, actual.supportedPrimerSessionIntents)
        assertEquals(expected.paymentMethodManagerCategories, actual.paymentMethodManagerCategories)
        assertEquals(expectedSurcharge, actual.surcharge)
    }

    @Test
    fun `toComposable maps all fields correctly with surcharge`() {
        val surcharge = mockk<Surcharge>()
        val surcharges = mapOf("PAYMENT_CARD" to surcharge)

        val headlessPaymentMethod = createHeadlessPaymentMethod(
            supportedIntents = listOf(PrimerSessionIntent.CHECKOUT, PrimerSessionIntent.VAULT),
        )

        val result = mapper.toComposable(headlessPaymentMethod, surcharges)

        assertPaymentMethodEquals(headlessPaymentMethod, result, surcharge)
    }

    @Test
    fun `toComposable maps correctly without surcharge`() {
        val headlessPaymentMethod = createHeadlessPaymentMethod(
            paymentMethodType = "GOOGLE_PAY",
            paymentMethodName = "Google Pay",
            categories = listOf(PrimerPaymentMethodManagerCategory.COMPONENT_WITH_REDIRECT),
        )

        val result = mapper.toComposable(headlessPaymentMethod, emptyMap())

        assertPaymentMethodEquals(headlessPaymentMethod, result)
    }

    @Test
    fun `toComposable maps correctly with empty surcharges map`() {
        val headlessPaymentMethod = createHeadlessPaymentMethod(
            paymentMethodType = "PAYPAL",
            paymentMethodName = "PayPal",
            supportedIntents = listOf(PrimerSessionIntent.VAULT),
            categories = listOf(PrimerPaymentMethodManagerCategory.COMPONENT_WITH_REDIRECT),
        )

        val result = mapper.toComposable(headlessPaymentMethod)

        assertPaymentMethodEquals(headlessPaymentMethod, result)
    }

    @Test
    fun `toComposable handles null payment method name`() {
        val headlessPaymentMethod = createHeadlessPaymentMethod(
            paymentMethodType = "KLARNA",
            paymentMethodName = null,
            supportedIntents = emptyList(),
            categories = emptyList(),
        )

        val result = mapper.toComposable(headlessPaymentMethod)

        assertPaymentMethodEquals(headlessPaymentMethod, result)
        assertNull(result.paymentMethodName)
    }

    @Test
    fun `toComposable with surcharge for different payment method type`() {
        val surcharge = mockk<Surcharge>()
        val surcharges = mapOf("GOOGLE_PAY" to surcharge)

        val headlessPaymentMethod = createHeadlessPaymentMethod(
            paymentMethodType = "APPLE_PAY",
            paymentMethodName = "Apple Pay",
        )

        val result = mapper.toComposable(headlessPaymentMethod, surcharges)

        assertPaymentMethodEquals(headlessPaymentMethod, result)
        assertNull(result.surcharge)
    }

    @Test
    fun `toComposable with multiple surcharges selects correct one`() {
        val surcharge1 = mockk<Surcharge>()
        val surcharge2 = mockk<Surcharge>()
        val surcharges = mapOf(
            "PAYMENT_CARD" to surcharge1,
            "GOOGLE_PAY" to surcharge2,
        )

        val headlessPaymentMethod = createHeadlessPaymentMethod(
            paymentMethodName = "Card",
        )

        val result = mapper.toComposable(headlessPaymentMethod, surcharges)

        assertPaymentMethodEquals(headlessPaymentMethod, result, surcharge1)
    }

    @Test
    fun `toComposable creates correct PrimerComposablePaymentMethod instance`() {
        val headlessPaymentMethod = createHeadlessPaymentMethod(
            paymentMethodType = "TEST_TYPE",
            paymentMethodName = "Test Payment",
            supportedIntents = listOf(PrimerSessionIntent.CHECKOUT, PrimerSessionIntent.VAULT),
            categories = listOf(
                PrimerPaymentMethodManagerCategory.NATIVE_UI,
                PrimerPaymentMethodManagerCategory.COMPONENT_WITH_REDIRECT,
            ),
        )

        val result = mapper.toComposable(headlessPaymentMethod)

        assertEquals(
            PrimerComposablePaymentMethod(
                paymentMethodType = headlessPaymentMethod.paymentMethodType,
                paymentMethodName = headlessPaymentMethod.paymentMethodName,
                supportedPrimerSessionIntents = headlessPaymentMethod.supportedPrimerSessionIntents,
                paymentMethodManagerCategories = headlessPaymentMethod.paymentMethodManagerCategories,
                surcharge = null,
            ),
            result,
        )
    }
}
