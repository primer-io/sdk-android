package io.primer.android.vault.implementation.vaultedMethods.data.datasource

import io.primer.android.vault.implementation.vaultedMethods.data.model.PaymentMethodVaultTokenInternal

internal class LocalVaultedPaymentMethodsDataSource :
    io.primer.android.core.data.datasource.BaseCacheDataSource<List<PaymentMethodVaultTokenInternal>,
        List<PaymentMethodVaultTokenInternal>> {

    private var vaultedPaymentMethodTokens: List<PaymentMethodVaultTokenInternal> = emptyList()

    override fun get() = vaultedPaymentMethodTokens.toList()

    override fun update(input: List<PaymentMethodVaultTokenInternal>) {
        this.vaultedPaymentMethodTokens = input
    }
}
