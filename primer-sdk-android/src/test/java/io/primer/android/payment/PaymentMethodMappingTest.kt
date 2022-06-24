package io.primer.android.payment

import io.primer.android.data.payments.methods.mapping.DefaultPaymentMethodMapping
import io.primer.android.data.configuration.models.CountryCode
import io.primer.android.data.configuration.models.OrderDataResponse
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.PrimerSettings
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

internal class PaymentMethodMappingTest {

    private val settings: PrimerSettings = PrimerSettings().apply {
        order = OrderDataResponse(
            amount = 50,
            currency = "USD",
            countryCode = CountryCode.US,
        )
    }

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
    fun `test maps paynl ideal correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.PAY_NL_IDEAL)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps paynl payconiq correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.PAY_NL_PAYCONIQ)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps paynl giropay correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.PAY_NL_GIROPAY)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps paynl p24 correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.PAY_NL_P24)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps paynl eps correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.PAY_NL_EPS)) {
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

    @Test
    fun `test maps adyen vipps correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.ADYEN_VIPPS)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen mobilepay correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.ADYEN_MOBILEPAY)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen ideal correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.ADYEN_IDEAL)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen dotpay correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.ADYEN_DOTPAY)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen blik transfer correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.ADYEN_BLIK)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }
//
//    @Test
//    fun `test maps adyen mbway transfer correctly`() {
//        val factory = DefaultPaymentMethodMapping(settings)
//        when (val result = factory.getPaymentMethodFor(PaymentMethodType.ADYEN_MBWAY)) {
//            is Failure -> Assert.fail()
//            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
//        }
//    }
//
//    @Test
//    fun `test maps adyen bank transfer correctly`() {
//        val factory = DefaultPaymentMethodMapping(settings)
//        when (val result = factory.getPaymentMethodFor(PaymentMethodType.ADYEN_BANK_TRANSFER)) {
//            is Failure -> Assert.fail()
//            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
//        }
//    }

    @Test
    fun `test maps adyen paytrail transfer correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.ADYEN_PAYTRAIL)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen payshop transfer correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.ADYEN_PAYSHOP)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps mollie bancontact correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.MOLLIE_BANCONTACT)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps mollie ideal correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.MOLLIE_IDEAL)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps mollie p24 correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.MOLLIE_P24)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps mollie eps correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.MOLLIE_EPS)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps buckaroo ideal correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.BUCKAROO_IDEAL)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps buckaroo eps correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.BUCKAROO_EPS)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps buckaroo sofort correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.BUCKAROO_SOFORT)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps buckaroo giropay correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.BUCKAROO_GIROPAY)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps buckaroo bancontact correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.BUCKAROO_BANCONTACT)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps atome correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.ATOME)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps 2c2p correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.TWOC2P)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps opennode correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.OPENNODE)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps rapyd gcash correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (val result = factory.getPaymentMethodFor(PaymentMethodType.RAPYD_GCASH)) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }
}
