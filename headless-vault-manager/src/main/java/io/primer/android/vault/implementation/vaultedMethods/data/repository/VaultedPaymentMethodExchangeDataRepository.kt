package io.primer.android.vault.implementation.vaultedMethods.data.repository

import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.core.data.model.BaseRemoteUrlRequest
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.vault.implementation.vaultedMethods.data.datasource.VaultedPaymentMethodExchangeDataSourceRegistry
import io.primer.android.vault.implementation.vaultedMethods.data.mapping.VaultedPaymentMethodAdditionalDataMapperRegistry
import io.primer.android.vault.implementation.vaultedMethods.domain.PrimerVaultedPaymentMethodAdditionalData
import io.primer.android.vault.implementation.vaultedMethods.domain.repository.VaultedPaymentMethodExchangeRepository

internal class VaultedPaymentMethodExchangeDataRepository(
    private val configurationDataSource: CacheConfigurationDataSource,
    private val vaultedPaymentMethodExchangeDataSourceRegistry: VaultedPaymentMethodExchangeDataSourceRegistry,
    private val vaultedPaymentMethodAdditionalDataMapperRegistry: VaultedPaymentMethodAdditionalDataMapperRegistry,
) : VaultedPaymentMethodExchangeRepository {
    override suspend fun exchangeVaultedPaymentToken(
        id: String,
        additionalData: PrimerVaultedPaymentMethodAdditionalData?,
    ) = runSuspendCatching {
        configurationDataSource.get()
            .let { configurationData ->
                vaultedPaymentMethodExchangeDataSourceRegistry.getExchangeDataSource(additionalData)
                    .execute(
                        BaseRemoteUrlRequest(
                            url = "${configurationData.pciUrl}/payment-instruments/$id/exchange",
                            data =
                                vaultedPaymentMethodAdditionalDataMapperRegistry.getMapper(
                                    additionalData,
                                ).map(additionalData),
                        ),
                    )
            }
    }
}
