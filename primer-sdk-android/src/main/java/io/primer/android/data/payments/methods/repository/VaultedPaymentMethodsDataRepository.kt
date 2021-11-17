package io.primer.android.data.payments.methods.repository

import io.primer.android.data.payments.methods.datasource.RemoteVaultedPaymentMethodsDataSource
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.domain.payments.methods.repository.VaultedPaymentMethodsRepository
import kotlinx.coroutines.flow.flatMapLatest

internal class VaultedPaymentMethodsDataRepository(
    private val remoteVaultedPaymentMethodsDataSource: RemoteVaultedPaymentMethodsDataSource,
    private val configurationDataSource: LocalConfigurationDataSource,
) : VaultedPaymentMethodsRepository {

    override fun getVaultedPaymentMethods() = configurationDataSource.get()
        .flatMapLatest {
            remoteVaultedPaymentMethodsDataSource.execute(it)
        }
}
