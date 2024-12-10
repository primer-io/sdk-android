package io.primer.android.components

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.configuration.data.model.PaymentMethodImplementationType
import io.primer.android.core.utils.Failure
import io.primer.android.core.utils.Success
import io.primer.android.googlepay.GooglePayFactory
import io.primer.android.ipay88.IPay88PaymentMethodFactory
import io.primer.android.klarna.KlarnaFactory
import io.primer.android.paymentmethods.PaymentMethod
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertFails

internal class PaymentMethodMappingTest {

    @RelaxedMockK
    internal lateinit var settings: PrimerSettings

    @RelaxedMockK
    internal lateinit var configurationDataSource: CacheConfigurationDataSource

    private lateinit var mapping: DefaultPaymentMethodMapping

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        mapping = DefaultPaymentMethodMapping(settings = settings, configurationDataSource = configurationDataSource)
    }

    @Test
    fun `test maps NATIVE_SDK failure correctly`() {
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
        mockkConstructor(KlarnaFactory::class)
        every { anyConstructed<KlarnaFactory>().build() } returns
            mockk<Success<PaymentMethod, Exception>> {
                every { value } returns mockk<PaymentMethod> {
                    every { type } returns PaymentMethodType.KLARNA.name
                }
            }
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.KLARNA.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertEquals(result.value.type, PaymentMethodType.KLARNA.name)
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
            is Success -> assertEquals(result.value.type, PaymentMethodType.PAYPAL.name)
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
            is Success -> assertEquals(result.value.type, PaymentMethodType.PAYMENT_CARD.name)
        }
    }

    @Test
    fun `test maps google pay correctly`() {
        mockkConstructor(GooglePayFactory::class)
        every { anyConstructed<GooglePayFactory>().build() } returns
            mockk<Success<PaymentMethod, Exception>> {
                every { value } returns mockk<PaymentMethod> {
                    every { type } returns PaymentMethodType.GOOGLE_PAY.name
                }
            }
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.GOOGLE_PAY.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertEquals(result.value.type, PaymentMethodType.GOOGLE_PAY.name)
        }
    }

    @Test
    fun `test maps adyen ideal correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.ADYEN_IDEAL.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertEquals(result.value.type, PaymentMethodType.ADYEN_IDEAL.name)
        }
    }

    @Test
    fun `test maps adyen dotpay correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.ADYEN_DOTPAY.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertEquals(result.value.type, PaymentMethodType.ADYEN_DOTPAY.name)
        }
    }

//    @Test
//    fun `test maps stripe ACH correctly`() {
//        when (
//            val result = mapping.getPaymentMethodFor(
//                PaymentMethodImplementationType.NATIVE_SDK,
//                PaymentMethodType.STRIPE_ACH.name
//            )
//        ) {
//            is Failure -> assertFails {}
//            is Success -> assertEquals(result.value.type, PaymentMethodType.STRIPE_ACH.name)
//        }
//    }

    @Test
    fun `test maps adyen bancontact correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.ADYEN_BANCONTACT_CARD.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertEquals(result.value.type, PaymentMethodType.ADYEN_BANCONTACT_CARD.name)
        }
    }

    @Test
    fun `test maps Xendit Ovo correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.XENDIT_OVO.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertEquals(result.value.type, PaymentMethodType.XENDIT_OVO.name)
        }
    }

    @Test
    fun `test maps Xfers Paynow correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.XFERS_PAYNOW.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertEquals(result.value.type, PaymentMethodType.XFERS_PAYNOW.name)
        }
    }

    @Disabled("RAPID_FAST supported removed temporarily")
    @Test
    fun `test maps RAPYD_FAST correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.RAPYD_FAST.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertEquals(result.value.type, PaymentMethodType.RAPYD_FAST.name)
        }
    }

    @Test
    fun `test maps RAPYD_PROMPTPAY correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.RAPYD_PROMPTPAY.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertEquals(result.value.type, PaymentMethodType.RAPYD_PROMPTPAY.name)
        }
    }

    @Test
    fun `test maps OMISE_PROMPTPAY correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.OMISE_PROMPTPAY.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertEquals(result.value.type, PaymentMethodType.OMISE_PROMPTPAY.name)
        }
    }

    @Test
    fun `test maps ADYEN_MULTIBANCO correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.ADYEN_MULTIBANCO.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertEquals(result.value.type, PaymentMethodType.ADYEN_MULTIBANCO.name)
        }
    }

    @Test
    fun `test maps XENDIT_RETAIL_OUTLETS correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.NATIVE_SDK,
                PaymentMethodType.XENDIT_RETAIL_OUTLETS.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertEquals(result.value.type, PaymentMethodType.XENDIT_RETAIL_OUTLETS.name)
        }
    }

//    @Test
//    fun `test maps NOL_PAY correctly`() {
//        when (
//            val result = mapping.getPaymentMethodFor(
//                PaymentMethodImplementationType.NATIVE_SDK,
//                PaymentMethodType.NOL_PAY.name
//            )
//        ) {
//            is Failure -> assertFails {}
//            is Success -> assertEquals(result.value.type, PaymentMethodType.NOL_PAY.name)
//        }
//    }

    @Test
    fun `test maps WEB_REDIRECT correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.WEB_REDIRECT,
                PaymentMethodType.HOOLAH.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertEquals(result.value.type, PaymentMethodType.HOOLAH.name)
        }
    }

    @Test
    fun `test maps IPAY88_SDK correctly`() {
        mockkConstructor(IPay88PaymentMethodFactory::class)
        every { anyConstructed<IPay88PaymentMethodFactory>().build() } returns
            mockk<Success<PaymentMethod, Exception>> {
                every { value } returns mockk<PaymentMethod> {
                    every { type } returns PaymentMethodType.IPAY88_CARD.name
                }
            }

        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.IPAY88_SDK,
                PaymentMethodType.IPAY88_CARD.name
            )
        ) {
            is Failure -> assertFails {}
            is Success -> assertEquals(PaymentMethodType.IPAY88_CARD.name, result.value.type)
        }
    }

    @Test
    fun `test maps UNKNOWN failure correctly`() {
        when (
            val result = mapping.getPaymentMethodFor(
                PaymentMethodImplementationType.UNKNOWN,
                PaymentMethodType.UNKNOWN.name
            )
        ) {
            is Failure -> {
                val msg = "Unknown payment method implementation UNKNOWN, can't register."
                assertEquals(msg, result.value.message)
            }

            is Success -> assertFails {}
        }
    }
}
