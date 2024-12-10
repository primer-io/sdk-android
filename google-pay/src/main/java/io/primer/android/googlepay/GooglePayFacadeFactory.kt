package io.primer.android.googlepay

import android.content.Context
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import io.primer.android.core.logging.internal.LogReporter

internal interface GooglePayFacadeFactory {
    fun create(
        applicationContext: Context,
        environment: GooglePayFacade.Environment,
        logReporter: LogReporter
    ): GooglePayFacade
}

internal class DefaultGooglePayFacadeFactory : GooglePayFacadeFactory {
    override fun create(
        applicationContext: Context,
        environment: GooglePayFacade.Environment,
        logReporter: LogReporter
    ): GooglePayFacade {
        val walletEnvironment =
            if (environment == GooglePayFacade.Environment.TEST) {
                WalletConstants.ENVIRONMENT_TEST
            } else {
                WalletConstants.ENVIRONMENT_PRODUCTION
            }
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(walletEnvironment)
            .build()
        val paymentsClient: PaymentsClient =
            Wallet.getPaymentsClient(applicationContext, walletOptions)

        return GooglePayFacade(paymentsClient, logReporter)
    }
}
