@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.di

import io.primer.android.data.rpc.retailOutlets.datasource.LocalRetailOutletDataSource
import io.primer.android.data.rpc.retailOutlets.datasource.RemoteRetailOutletFlowDataSource
import io.primer.android.data.rpc.retailOutlets.repository.RetailOutletDataRepository
import io.primer.android.domain.rpc.retailOutlets.RetailOutletFilterInteractor
import io.primer.android.domain.rpc.retailOutlets.RetailOutletInteractor
import io.primer.android.domain.rpc.retailOutlets.repository.RetailOutletRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi

internal class RetailOutletsContainer(private val sdk: SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton { LocalRetailOutletDataSource() }

        registerSingleton { RemoteRetailOutletFlowDataSource(sdk.resolve()) }

        registerSingleton<RetailOutletRepository> {
            RetailOutletDataRepository(
                resolve(),
                resolve(),
                sdk.resolve()
            )
        }

        registerSingleton { RetailOutletInteractor(resolve()) }

        registerSingleton { RetailOutletFilterInteractor(resolve()) }
    }
}
