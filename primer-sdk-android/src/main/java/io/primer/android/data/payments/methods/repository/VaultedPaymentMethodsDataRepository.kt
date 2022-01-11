package io.primer.android.data.payments.methods.repository

import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.payments.methods.datasource.RemoteVaultedPaymentMethodsDataSource
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.payments.methods.datasource.RemoteVaultedPaymentMethodDeleteDataSource
import io.primer.android.data.payments.methods.datasource.RemoteVaultedPaymentMethodsExchangeDataSource
import io.primer.android.domain.payments.methods.repository.VaultedPaymentMethodsRepository
import kotlinx.coroutines.flow.flatMapLatest

internal class VaultedPaymentMethodsDataRepository(
    private val remoteVaultedPaymentMethodsDataSource: RemoteVaultedPaymentMethodsDataSource,
    private val vaultedPaymentMethodDeleteDataSource: RemoteVaultedPaymentMethodDeleteDataSource,
    private val exchangePaymentMethodDataSource: RemoteVaultedPaymentMethodsExchangeDataSource,
    private val configurationDataSource: LocalConfigurationDataSource,
) : VaultedPaymentMethodsRepository {

    override fun getVaultedPaymentMethods() = configurationDataSource.get()
        .flatMapLatest {
            remoteVaultedPaymentMethodsDataSource.execute(it)
        }

    override fun exchangeVaultedPaymentToken(id: String) = configurationDataSource.get()
        .flatMapLatest {
            exchangePaymentMethodDataSource.execute(BaseRemoteRequest(it, id))
        }

    override fun deleteVaultedPaymentMethod(id: String) = configurationDataSource.get()
        .flatMapLatest {
            vaultedPaymentMethodDeleteDataSource.execute(BaseRemoteRequest(it, id))
        }
}
