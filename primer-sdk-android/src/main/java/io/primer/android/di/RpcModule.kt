package io.primer.android.di

import io.primer.android.data.rpc.banks.datasource.LocalIssuingBankDataSource
import io.primer.android.data.rpc.banks.datasource.RemoteIssuingBankFlowDataSource
import io.primer.android.data.rpc.banks.repository.IssuingBankDataRepository
import io.primer.android.domain.rpc.banks.BanksFilterInteractor
import io.primer.android.domain.rpc.banks.BanksInteractor
import io.primer.android.domain.rpc.banks.repository.IssuingBankRepository
import io.primer.android.viewmodel.bank.BankSelectionViewModel
import io.primer.android.viewmodel.bank.DotPayBankSelectionViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal const val BANK_SELECTOR_SCOPE = "BANK_SELECTOR_SCOPE"
internal val rpcModule = {
    module {
        scope(named(BANK_SELECTOR_SCOPE)) {
            scoped { LocalIssuingBankDataSource() }
            scoped { RemoteIssuingBankFlowDataSource(get()) }
            scoped<IssuingBankRepository> { IssuingBankDataRepository(get(), get(), get()) }
            scoped { BanksInteractor(get()) }
            scoped { BanksFilterInteractor(get()) }
        }

        viewModel { BankSelectionViewModel(getScope(BANK_SELECTOR_SCOPE).get(), get()) }
        viewModel {
            DotPayBankSelectionViewModel(
                getScope(BANK_SELECTOR_SCOPE).get(),
                getScope(BANK_SELECTOR_SCOPE).get(),
                get()
            )
        }
    }
}
