@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.di

import io.primer.android.data.rpc.banks.datasource.LocalIssuingBankDataSource
import io.primer.android.data.rpc.banks.datasource.RemoteIssuingBankSuspendDataSource
import io.primer.android.data.rpc.banks.repository.IssuingBankDataRepository
import io.primer.android.domain.rpc.banks.BanksFilterInteractor
import io.primer.android.domain.rpc.banks.BanksInteractor
import io.primer.android.domain.rpc.banks.repository.IssuingBankRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi

internal class RpcContainer(private val sdk: SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton { LocalIssuingBankDataSource() }

        registerSingleton { RemoteIssuingBankSuspendDataSource(sdk.resolve()) }

        registerSingleton<IssuingBankRepository> {
            IssuingBankDataRepository(resolve(), resolve(), sdk.resolve())
        }

        registerSingleton { BanksInteractor(resolve()) }

        registerSingleton { BanksFilterInteractor(resolve()) }
    }
}
