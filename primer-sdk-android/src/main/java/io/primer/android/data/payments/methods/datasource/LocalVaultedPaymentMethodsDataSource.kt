package io.primer.android.data.payments.methods.datasource

import io.primer.android.data.base.datasource.BaseCacheDataSource
import io.primer.android.data.payments.methods.models.PaymentMethodVaultTokenInternal

internal class LocalVaultedPaymentMethodsDataSource :
    BaseCacheDataSource<List<PaymentMethodVaultTokenInternal>,
        List<PaymentMethodVaultTokenInternal>> {

    private var vaultedPaymentMethodTokens: List<PaymentMethodVaultTokenInternal> = emptyList()

    override fun get() = vaultedPaymentMethodTokens.toList()

    override fun update(input: List<PaymentMethodVaultTokenInternal>) {
        this.vaultedPaymentMethodTokens = input
    }
}
