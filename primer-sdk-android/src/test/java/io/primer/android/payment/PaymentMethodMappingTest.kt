package io.primer.android.payment

import io.primer.android.data.configuration.models.CountryCode
import io.primer.android.data.configuration.models.OrderDataResponse
import io.primer.android.data.configuration.models.PaymentMethodImplementationType
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.methods.mapping.DefaultPaymentMethodMapping
import io.primer.android.data.settings.PrimerSettings
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
            merchantAmount = 50,
            currencyCode = "USD",
            countryCode = CountryCode.US
        )
    }

    @Test
    fun `test maps failure correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.UNKNOWN.name
            )
        ) {
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
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.KLARNA.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is Klarna)
        }
    }

    @Test
    fun `test maps paypal correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.PAYPAL.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is PayPal)
        }
    }

    @Test
    fun `test maps card correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.PAYMENT_CARD.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is Card)
        }
    }

    @Test
    fun `test maps google pay correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.GOOGLE_PAY.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is GooglePay)
        }
    }

    @Test
    fun `test maps hoolah correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.HOOLAH.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps paynl ideal correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.PAY_NL_IDEAL.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps paynl payconiq correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.PAY_NL_PAYCONIQ.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps paynl giropay correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.PAY_NL_GIROPAY.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps paynl p24 correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.PAY_NL_P24.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps paynl eps correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.PAY_NL_EPS.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen sofort correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.ADYEN_SOFORT.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen alipay correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.ADYEN_ALIPAY.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen trustly correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.ADYEN_TRUSTLY.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen twint correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.ADYEN_TWINT.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen giropay correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.ADYEN_GIROPAY.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen vipps correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.ADYEN_VIPPS.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen mobilepay correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.ADYEN_MOBILEPAY.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen ideal correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.ADYEN_IDEAL.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen dotpay correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.ADYEN_DOTPAY.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen blik transfer correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.ADYEN_BLIK.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen mbway transfer correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.ADYEN_MBWAY.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

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
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.ADYEN_PAYTRAIL.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen payshop transfer correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.ADYEN_PAYSHOP.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps mollie bancontact correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.MOLLIE_BANCONTACT.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps mollie ideal correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.MOLLIE_IDEAL.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps mollie p24 correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.MOLLIE_P24.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps mollie eps correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.MOLLIE_EPS.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps buckaroo ideal correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.BUCKAROO_IDEAL.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps buckaroo eps correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.BUCKAROO_EPS.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps buckaroo sofort correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.BUCKAROO_SOFORT.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps buckaroo giropay correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.BUCKAROO_GIROPAY.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps buckaroo bancontact correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.BUCKAROO_BANCONTACT.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps atome correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.ATOME.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps 2c2p correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.TWOC2P.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps opennode correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.OPENNODE.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps rapyd gcash correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.RAPYD_GCASH.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps grab pay rapyd correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.RAPYD_GRABPAY.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps rapyd poli correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.RAPYD_POLI.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps rapyd fast correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.RAPYD_FAST.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps rapyd promptpay correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.RAPYD_PROMPTPAY.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen multibanco correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.ADYEN_MULTIBANCO.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps omise promptpay correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.OMISE_PROMPTPAY.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen bancontact correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.ADYEN_BANCONTACT_CARD.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps xendit retail outlets correctly`() {
        val factory = DefaultPaymentMethodMapping(settings)
        when (
            val result = factory.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.XENDIT_RETAIL_OUTLETS.name
            )
        ) {
            is Failure -> Assert.fail()
            is Success -> Assert.assertTrue(result.value is AsyncPaymentMethod)
        }
    }
}
