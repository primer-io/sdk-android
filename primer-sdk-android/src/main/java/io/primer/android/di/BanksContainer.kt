package io.primer.android.di

import io.primer.android.components.data.payments.paymentMethods.componentWithRedirect.banks.error.BanksErrorMapper
import io.primer.android.domain.error.ErrorMapper

internal class BanksContainer : DependencyContainer() {
    override fun registerInitialDependencies() {
        registerFactory<ErrorMapper>(BANKS_ERROR_RESOLVER_NAME) {
            BanksErrorMapper()
        }
    }

    internal companion object {
        const val BANKS_ERROR_RESOLVER_NAME = "banksErrorResolver"
    }
}
