package io.primer.android.payment.google

import android.content.Context
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import io.primer.android.PaymentMethodModule
import io.primer.android.payment.GOOGLE_PAY_IDENTIFIER
import io.primer.android.payment.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.viewmodel.GooglePayPaymentMethodChecker
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry

class GoogleModule : PaymentMethodModule {

    private lateinit var googlePayBridge: GooglePayBridge

    override fun initialize(applicationContext: Context) {
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
            .build()
        val paymentsClient: PaymentsClient =
            Wallet.getPaymentsClient(applicationContext, walletOptions)

        googlePayBridge = GooglePayBridge(paymentsClient)
    }

    override fun registerPaymentMethodCheckers(
        paymentMethodCheckerRegistry: PaymentMethodCheckerRegistry,
    ) {
        val googlePayChecker = GooglePayPaymentMethodChecker(googlePayBridge)

        paymentMethodCheckerRegistry.register(
            GOOGLE_PAY_IDENTIFIER,
            googlePayChecker
        )
    }

    override fun registerPaymentMethodDescriptorFactory(
        paymentMethodDescriptorFactoryRegistry: PaymentMethodDescriptorFactoryRegistry,
    ) {
        val paymentMethodDescriptorFactory =
            GooglePayPaymentMethodDescriptorFactory(googlePayBridge)

        paymentMethodDescriptorFactoryRegistry.register(
            GOOGLE_PAY_IDENTIFIER,
            paymentMethodDescriptorFactory
        )
    }
}
