package io.primer.android.payment

import io.primer.android.PaymentMethod
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.payment.card.CreditCard
import io.primer.android.payment.gocardless.GoCardless
import io.primer.android.payment.google.GooglePayBridge
import io.primer.android.payment.google.GooglePayDescriptor
import io.primer.android.payment.klarna.Klarna
import io.primer.android.payment.paypal.PayPal
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry

// TODO rename to PaymentMethodDescriptorFactoryRegistry
internal class PaymentMethodDescriptorFactory(
    private val paymentMethodCheckers: PaymentMethodCheckerRegistry,
) {

    private val factories: MutableMap<String, SinglePaymentMethodDescriptorFactory> = mutableMapOf()

    fun create(
        checkoutConfig: CheckoutConfig,
        paymentMethodRemoteConfig: PaymentMethodRemoteConfig,
        paymentMethod: PaymentMethod,
    ): PaymentMethodDescriptor? =
        factories[paymentMethodRemoteConfig.type]?.create(
            checkoutConfig = checkoutConfig,
            paymentMethodRemoteConfig = paymentMethodRemoteConfig,
            paymentMethod = paymentMethod,
            paymentMethodCheckers = paymentMethodCheckers
        )

    fun register(id: String, factory: SinglePaymentMethodDescriptorFactory) {
        factories[id] = factory
    }

    fun unregister(id: SinglePaymentMethodDescriptorFactory) {
        factories.remove(id)
    }

    operator fun get(id: String): SinglePaymentMethodDescriptorFactory? = factories[id]
}

internal interface SinglePaymentMethodDescriptorFactory {

    fun create(
        checkoutConfig: CheckoutConfig,
        paymentMethodRemoteConfig: PaymentMethodRemoteConfig,
        paymentMethod: PaymentMethod,
        paymentMethodCheckers: PaymentMethodCheckerRegistry,
    ): PaymentMethodDescriptor
}

internal class GooglePayPaymentMethodDescriptorFactory(
    private val googlePayBridge: GooglePayBridge,
) : SinglePaymentMethodDescriptorFactory {

    override fun create(
        checkoutConfig: CheckoutConfig,
        paymentMethodRemoteConfig: PaymentMethodRemoteConfig,
        paymentMethod: PaymentMethod,
        paymentMethodCheckers: PaymentMethodCheckerRegistry,
    ): GooglePayDescriptor =
        GooglePayDescriptor(
            checkoutConfig = checkoutConfig,
            options = paymentMethod as PaymentMethod.GooglePay,
            paymentMethodChecker = paymentMethodCheckers[GOOGLE_PAY_IDENTIFIER]
                ?: throw Error("Missing payment method checker"),
            googlePayBridge = googlePayBridge,
            config = paymentMethodRemoteConfig
        )
}

internal class CardPaymentMethodDescriptorFactory : SinglePaymentMethodDescriptorFactory {

    override fun create(
        checkoutConfig: CheckoutConfig,
        paymentMethodRemoteConfig: PaymentMethodRemoteConfig,
        paymentMethod: PaymentMethod,
        paymentMethodCheckers: PaymentMethodCheckerRegistry,
    ): PaymentMethodDescriptor =
        CreditCard(
            config = paymentMethodRemoteConfig,
            options = paymentMethod as PaymentMethod.Card
        )
}

internal class PayPalPaymentMethodDescriptorFactory : SinglePaymentMethodDescriptorFactory {

    override fun create(
        checkoutConfig: CheckoutConfig,
        paymentMethodRemoteConfig: PaymentMethodRemoteConfig,
        paymentMethod: PaymentMethod,
        paymentMethodCheckers: PaymentMethodCheckerRegistry,
    ): PaymentMethodDescriptor =
        PayPal(
            paymentMethodRemoteConfig,
            paymentMethod as PaymentMethod.PayPal
        )
}

internal class GoCardlessPaymentMethodDescriptorFactory : SinglePaymentMethodDescriptorFactory {

    override fun create(
        checkoutConfig: CheckoutConfig,
        paymentMethodRemoteConfig: PaymentMethodRemoteConfig,
        paymentMethod: PaymentMethod,
        paymentMethodCheckers: PaymentMethodCheckerRegistry,
    ): PaymentMethodDescriptor =
        GoCardless(
            paymentMethodRemoteConfig,
            paymentMethod as PaymentMethod.GoCardless
        )
}

internal class KlarnaPaymentMethodDescriptorFactory : SinglePaymentMethodDescriptorFactory {

    override fun create(
        checkoutConfig: CheckoutConfig,
        paymentMethodRemoteConfig: PaymentMethodRemoteConfig,
        paymentMethod: PaymentMethod,
        paymentMethodCheckers: PaymentMethodCheckerRegistry,
    ): PaymentMethodDescriptor =
        Klarna(
            checkoutConfig,
            paymentMethod as PaymentMethod.Klarna,
            paymentMethodRemoteConfig
        )
}

