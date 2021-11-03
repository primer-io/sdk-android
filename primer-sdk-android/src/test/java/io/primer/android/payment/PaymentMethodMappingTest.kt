package io.primer.android.payment

import io.primer.android.data.payments.methods.mapping.DefaultPaymentMethodMapping
import io.primer.android.model.dto.CountryCode
import io.primer.android.model.dto.Order
import io.primer.android.model.dto.PaymentMethodType
import io.primer.android.model.dto.PrimerSettings
import io.primer.android.payment.apaya.Apaya
import io.primer.android.payment.async.AsyncPaymentMethod
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
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.UNKNOWN)) {
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
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.KLARNA)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is Klarna)
        }
    }

    @Test
    fun `test maps paypal correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.PAYPAL)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is PayPal)
        }
    }

    @Test
    fun `test maps card correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.PAYMENT_CARD)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is Card)
        }
    }

    @Test
    fun `test maps google pay correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.GOOGLE_PAY)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is GooglePay)
        }
    }

    @Test
    fun `test maps apaya correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.APAYA)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is Apaya)
        }
    }

    @Test
    fun `test maps hoolah correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.HOOLAH)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps ideal correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.PAY_NL_IDEAL)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen sofort correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.ADYEN_SOFORT)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen alipay correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.ADYEN_ALIPAY)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen trustly correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.ADYEN_TRUSTLY)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen twint correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.ADYEN_TWINT)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen giropay correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.ADYEN_GIROPAY)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }
}
