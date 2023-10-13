package io.primer.android.viewmodel.bank

import androidx.lifecycle.viewModelScope
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.di.DISdkComponent
import io.primer.android.domain.rpc.banks.BanksFilterInteractor
import io.primer.android.domain.rpc.banks.BanksInteractor
import io.primer.android.domain.rpc.banks.models.IssuingBankFilterParams
import io.primer.android.ui.BankItem
import kotlinx.coroutines.launch

internal class DotPayBankSelectionViewModel(
    interactor: BanksInteractor,
    private val banksFilterInteractor: BanksFilterInteractor,
    private val analyticsInteractor: AnalyticsInteractor
) : BankSelectionViewModel(interactor, analyticsInteractor), DISdkComponent {

    fun onFilterChanged(
        text: String
    ) = viewModelScope.launch {
        banksFilterInteractor(
            IssuingBankFilterParams(text)
        ).collect {
            _itemsLiveData.postValue(it.map { BankItem(it.id, it.name, it.iconUrl) })
        }
    }
}
