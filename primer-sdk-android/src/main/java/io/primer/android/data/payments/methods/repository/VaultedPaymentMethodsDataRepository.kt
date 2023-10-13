package io.primer.android.data.payments.methods.repository

import io.primer.android.components.domain.exception.VaultManagerDeleteException
import io.primer.android.components.domain.exception.VaultManagerFetchException
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.payments.methods.datasource.LocalVaultedPaymentMethodsDataSource
import io.primer.android.data.payments.methods.datasource.RemoteVaultedPaymentMethodDeleteDataSource
import io.primer.android.data.payments.methods.datasource.RemoteVaultedPaymentMethodsDataSource
import io.primer.android.domain.payments.methods.repository.VaultedPaymentMethodsRepository
import io.primer.android.extensions.onError
import io.primer.android.extensions.runSuspendCatching
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.IOException

@ExperimentalCoroutinesApi
internal class VaultedPaymentMethodsDataRepository(
    private val remoteVaultedPaymentMethodsDataSource: RemoteVaultedPaymentMethodsDataSource,
    private val localVaultedPaymentMethodsDataSource: LocalVaultedPaymentMethodsDataSource,
    private val vaultedPaymentMethodDeleteDataSource: RemoteVaultedPaymentMethodDeleteDataSource,
    private val configurationDataSource: LocalConfigurationDataSource
) : VaultedPaymentMethodsRepository {

    override suspend fun getVaultedPaymentMethods(fromCache: Boolean) = runSuspendCatching {
        when (fromCache) {
            true -> localVaultedPaymentMethodsDataSource.get()
            false -> configurationDataSource.getConfiguration().let { configurationData ->
                remoteVaultedPaymentMethodsDataSource.execute(configurationData)
            }.also { vaultedTokens ->
                localVaultedPaymentMethodsDataSource.update(vaultedTokens)
            }
        }
    }.onError { throwable ->
        throw when (throwable) {
            !is IOException -> VaultManagerFetchException(throwable.message)
            else -> throwable
        }
    }

    override suspend fun deleteVaultedPaymentMethod(id: String) = runSuspendCatching {
        configurationDataSource.getConfiguration()
            .let { configurationData ->
                vaultedPaymentMethodDeleteDataSource.execute(
                    BaseRemoteRequest(
                        configurationData,
                        id
                    )
                )
            }.also {
                localVaultedPaymentMethodsDataSource.update(
                    localVaultedPaymentMethodsDataSource.get().filter { it.token == id }
                )
            }
    }.onError { throwable ->
        throw when (throwable) {
            !is IOException -> VaultManagerDeleteException(throwable.message)
            else -> throwable
        }
    }
}
