package io.primer.android.payment

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.CompleteStripeAchPaymentSessionDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.StripeAchMandateTimestampLoggingDelegate
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.models.PaymentMethodImplementationType
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.methods.mapping.DefaultPaymentMethodMapping
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.error.CheckoutErrorEventResolver
import io.primer.android.domain.payments.create.repository.PaymentResultRepository
import io.primer.android.events.EventDispatcher
import io.primer.android.payment.async.AsyncPaymentMethod
import io.primer.android.payment.card.Card
import io.primer.android.payment.google.GooglePay
import io.primer.android.payment.klarna.Klarna
import io.primer.android.payment.paypal.PayPal
import io.primer.android.utils.Failure
import io.primer.android.utils.Success
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

// TODO TWS: test for ACH factory
internal class PaymentMethodMappingTest {

    @RelaxedMockK
    internal lateinit var settings: PrimerSettings

    @RelaxedMockK
    internal lateinit var localConfigurationDataSource: LocalConfigurationDataSource

    @MockK
    private lateinit var eventDispatcher: EventDispatcher

    @MockK
    private lateinit var paymentResultRepository: PaymentResultRepository

    @MockK
    private lateinit var checkoutErrorEventResolver: CheckoutErrorEventResolver

    @MockK
    private lateinit var completeStripeAchPaymentSessionDelegate:
        CompleteStripeAchPaymentSessionDelegate

    @MockK
    private lateinit var stripeAchMandateTimestampLoggingDelegate:
        StripeAchMandateTimestampLoggingDelegate

    private lateinit var mapping: DefaultPaymentMethodMapping

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        mapping = DefaultPaymentMethodMapping(
            settings,
            localConfigurationDataSource,
            eventDispatcher,
            paymentResultRepository,
            checkoutErrorEventResolver,
            completeStripeAchPaymentSessionDelegate,
            stripeAchMandateTimestampLoggingDelegate
        )
    }

    @Test
    fun `test maps failure correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.UNKNOWN.name
            )
        ) {
            is Failure -> {
                val msg = "Unknown payment method, can't register."
                assertEquals(result.value.message, msg)
            }

            is Success -> assertFails {}
        }
    }

    @Test
    fun `test maps klarna correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.KLARNA.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is Klarna)
        }
    }

    @Test
    fun `test maps paypal correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.PAYPAL.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is PayPal)
        }
    }

    @Test
    fun `test maps card correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.PAYMENT_CARD.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is Card)
        }
    }

    @Test
    fun `test maps google pay correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.GOOGLE_PAY.name
            )
        ) {
            is Failure -> {}
            is Success -> assertTrue(result.value is GooglePay)
        }
    }

    @Test
    fun `test maps hoolah correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.HOOLAH.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps paynl ideal correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.PAY_NL_IDEAL.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps paynl payconiq correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.PAY_NL_PAYCONIQ.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps paynl giropay correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.PAY_NL_GIROPAY.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps paynl p24 correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.PAY_NL_P24.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps paynl eps correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.PAY_NL_EPS.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen sofort correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.ADYEN_SOFORT.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen alipay correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.ADYEN_ALIPAY.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen trustly correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.ADYEN_TRUSTLY.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen twint correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.ADYEN_TWINT.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen giropay correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.ADYEN_GIROPAY.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen vipps correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.ADYEN_VIPPS.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen mobilepay correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.ADYEN_MOBILEPAY.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen ideal correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.ADYEN_IDEAL.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen dotpay correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.ADYEN_DOTPAY.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen blik transfer correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.ADYEN_BLIK.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen mbway transfer correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.ADYEN_MBWAY.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen paytrail transfer correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.ADYEN_PAYTRAIL.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen payshop transfer correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.ADYEN_PAYSHOP.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps mollie bancontact correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.MOLLIE_BANCONTACT.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps mollie ideal correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.MOLLIE_IDEAL.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps mollie p24 correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.MOLLIE_P24.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps mollie eps correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.MOLLIE_EPS.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps buckaroo ideal correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.BUCKAROO_IDEAL.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps buckaroo eps correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.BUCKAROO_EPS.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps buckaroo sofort correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.BUCKAROO_SOFORT.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps buckaroo giropay correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.BUCKAROO_GIROPAY.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps buckaroo bancontact correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.BUCKAROO_BANCONTACT.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps atome correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.ATOME.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps 2c2p correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.TWOC2P.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps opennode correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.OPENNODE.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps rapyd gcash correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.RAPYD_GCASH.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps grab pay rapyd correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.RAPYD_GRABPAY.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps rapyd poli correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.RAPYD_POLI.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps rapyd fast correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.RAPYD_FAST.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps rapyd promptpay correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.RAPYD_PROMPTPAY.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen multibanco correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.ADYEN_MULTIBANCO.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps omise promptpay correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.OMISE_PROMPTPAY.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps adyen bancontact correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.ADYEN_BANCONTACT_CARD.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }

    @Test
    fun `test maps xendit retail outlets correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.XENDIT_RETAIL_OUTLETS.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertTrue(result.value is AsyncPaymentMethod)
        }
    }
}
