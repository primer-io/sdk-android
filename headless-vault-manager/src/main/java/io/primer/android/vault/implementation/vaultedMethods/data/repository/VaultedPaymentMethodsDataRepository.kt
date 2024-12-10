package io.primer.android.vault.implementation.vaultedMethods.data.repository

import io.primer.android.components.domain.exception.VaultManagerDeleteException
import io.primer.android.components.domain.exception.VaultManagerFetchException
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.extensions.onError
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.vault.implementation.vaultedMethods.data.datasource.LocalVaultedPaymentMethodsDataSource
import io.primer.android.vault.implementation.vaultedMethods.data.datasource.RemoteVaultedPaymentMethodDeleteDataSource
import io.primer.android.vault.implementation.vaultedMethods.data.datasource.RemoteVaultedPaymentMethodsDataSource
import io.primer.android.vault.implementation.vaultedMethods.domain.repository.VaultedPaymentMethodsRepository

import java.io.IOException

internal class VaultedPaymentMethodsDataRepository(
    private val remoteVaultedPaymentMethodsDataSource: RemoteVaultedPaymentMethodsDataSource,
    private val localVaultedPaymentMethodsDataSource: LocalVaultedPaymentMethodsDataSource,
    private val vaultedPaymentMethodDeleteDataSource: RemoteVaultedPaymentMethodDeleteDataSource,
    private val configurationDataSource: CacheConfigurationDataSource
) : VaultedPaymentMethodsRepository {

    override suspend fun getVaultedPaymentMethods(fromCache: Boolean) = runSuspendCatching {
        when (fromCache) {
            true -> localVaultedPaymentMethodsDataSource.get()
            false -> configurationDataSource.get().let { configurationData ->
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
        configurationDataSource.get()
            .let { configurationData ->
                vaultedPaymentMethodDeleteDataSource.execute(
                    BaseRemoteHostRequest(
                        configurationData.pciUrl,
                        id
                    )
                )
            }.also {
                localVaultedPaymentMethodsDataSource.update(
                    localVaultedPaymentMethodsDataSource.get().filter { it.token != id }
                )
            }
    }.onError { throwable ->
        throw when (throwable) {
            !is IOException -> VaultManagerDeleteException(throwable.message)
            else -> throwable
        }
    }
}
