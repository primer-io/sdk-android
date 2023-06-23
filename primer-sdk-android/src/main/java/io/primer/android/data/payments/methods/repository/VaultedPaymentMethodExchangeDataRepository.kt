package io.primer.android.data.payments.methods.repository

import io.primer.android.components.domain.payments.vault.PrimerVaultedPaymentMethodAdditionalData
import io.primer.android.data.base.models.BaseRemoteUrlRequest
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.payments.methods.datasource.VaultedPaymentMethodExchangeDataSourceRegistry
import io.primer.android.data.payments.methods.mapping.vault.VaultedPaymentMethodAdditionalDataMapperRegistry
import io.primer.android.domain.payments.methods.repository.VaultedPaymentMethodExchangeRepository
import kotlinx.coroutines.flow.flatMapLatest

internal class VaultedPaymentMethodExchangeDataRepository(
    private val configurationDataSource: LocalConfigurationDataSource,
    private val vaultedPaymentMethodExchangeDataSourceRegistry:
        VaultedPaymentMethodExchangeDataSourceRegistry,
    private val vaultedPaymentMethodAdditionalDataMapperRegistry:
        VaultedPaymentMethodAdditionalDataMapperRegistry
) : VaultedPaymentMethodExchangeRepository {

    override fun exchangeVaultedPaymentToken(
        id: String,
        additionalData: PrimerVaultedPaymentMethodAdditionalData?
    ) = configurationDataSource.get()
        .flatMapLatest { configurationData ->
            vaultedPaymentMethodExchangeDataSourceRegistry.getExchangeDataSource(additionalData)
                .execute(
                    BaseRemoteUrlRequest(
                        "${configurationData.pciUrl}/payment-instruments/$id/exchange",
                        vaultedPaymentMethodAdditionalDataMapperRegistry.getMapper(
                            additionalData
                        ).map(additionalData)
                    )
                )
        }
}
