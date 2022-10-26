package io.primer.android.di

import io.primer.android.data.rpc.retailOutlets.datasource.LocalRetailOutletDataSource
import io.primer.android.data.rpc.retailOutlets.datasource.RemoteRetailOutletFlowDataSource
import io.primer.android.data.rpc.retailOutlets.repository.RetailOutletDataRepository
import io.primer.android.domain.rpc.retailOutlets.RetailOutletFilterInteractor
import io.primer.android.domain.rpc.retailOutlets.RetailOutletInteractor
import io.primer.android.domain.rpc.retailOutlets.repository.RetailOutletRepository
import org.koin.dsl.module

internal val retailOutletsModule = {
    module {
        single { LocalRetailOutletDataSource() }
        single { RemoteRetailOutletFlowDataSource(get()) }
        single<RetailOutletRepository> { RetailOutletDataRepository(get(), get(), get()) }
        single { RetailOutletInteractor(get()) }
        single { RetailOutletFilterInteractor(get()) }
    }
}
