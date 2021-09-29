package io.primer.android.payment

import io.primer.android.model.dto.CountryCode
import io.primer.android.model.dto.Order
import io.primer.android.model.dto.PrimerSettings
import io.primer.android.payment.card.Card
import io.primer.android.payment.google.GooglePay
import io.primer.android.payment.klarna.Klarna
import io.primer.android.payment.paypal.PayPal
import io.primer.android.utils.Failure
import io.primer.android.utils.Success
import org.junit.Assert
import org.junit.Test

class PaymentMethodMappingTest {

    private val settings: PrimerSettings = PrimerSettings(
        order = Order(
            amount = 200,
            currency = "USD",
            countryCode = CountryCode.US,
        )
    )

    @Test
    fun `test maps failure correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor("FOO")) {
            is Failure -> {
                val msg = "Unknown payment method, can't register."
                Assert.assertEquals(result.value.message, msg)
            }
            is Success -> Assert.fail()
        }
    }

    @Test
    fun `test maps klarna correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor("KLARNA")) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is Klarna)
        }
    }

    @Test
    fun `test maps paypal correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor("PAYPAL")) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is PayPal)
        }
    }

    @Test
    fun `test maps card correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor("PAYMENT_CARD")) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is Card)
        }
    }

    @Test
    fun `test maps google pay correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor("GOOGLE_PAY")) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is GooglePay)
        }
    }
}
