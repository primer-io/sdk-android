package io.primer.android.banks.di

import io.primer.android.banks.implementation.rpc.data.datasource.LocalIssuingBankDataSource
import io.primer.android.banks.implementation.rpc.data.datasource.RemoteIssuingBankSuspendDataSource
import io.primer.android.banks.implementation.rpc.data.repository.IssuingBankDataRepository
import io.primer.android.banks.implementation.rpc.domain.BanksFilterInteractor
import io.primer.android.banks.implementation.rpc.domain.BanksInteractor
import io.primer.android.banks.implementation.rpc.domain.repository.IssuingBankRepository
import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer

internal class RpcContainer(private val sdk: () -> SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton { LocalIssuingBankDataSource() }

        registerSingleton { RemoteIssuingBankSuspendDataSource(primerHttpClient = sdk().resolve()) }

        registerSingleton<IssuingBankRepository> {
            IssuingBankDataRepository(
                remoteIssuingSuspendDataSource = resolve(),
                localIssuingBankDataSource = resolve(),
                configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY)
            )
        }

        registerSingleton { BanksInteractor(issuingBankRepository = resolve()) }

        registerSingleton { BanksFilterInteractor(issuingBankRepository = resolve()) }
    }
}
