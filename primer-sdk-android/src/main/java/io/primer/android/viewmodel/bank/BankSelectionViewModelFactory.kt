package io.primer.android.viewmodel.bank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.domain.rpc.banks.BanksInteractor

internal class BankSelectionViewModelFactory(
    private val interactor: BanksInteractor,
    private val analyticsInteractor: AnalyticsInteractor
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return BankSelectionViewModel(
            interactor,
            analyticsInteractor
        ) as T
    }
}
