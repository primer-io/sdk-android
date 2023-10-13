package io.primer.android.viewmodel.bank

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.primer.android.analytics.data.models.AnalyticsAction
import io.primer.android.analytics.data.models.ObjectId
import io.primer.android.analytics.data.models.ObjectType
import io.primer.android.analytics.data.models.Place
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.BankIssuerContextParams
import io.primer.android.analytics.domain.models.UIAnalyticsParams
import io.primer.android.di.DISdkComponent
import io.primer.android.domain.rpc.banks.BanksInteractor
import io.primer.android.domain.rpc.banks.models.IssuingBankParams
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.presentation.base.BaseViewModel
import io.primer.android.ui.BankItem
import io.primer.android.ui.BaseBankItem
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

internal open class BankSelectionViewModel(
    private val interactor: BanksInteractor,
    analyticsInteractor: AnalyticsInteractor
) : BaseViewModel(analyticsInteractor), DISdkComponent {

    protected val _itemsLiveData = MutableLiveData<List<BaseBankItem>>()
    val itemsLiveData: LiveData<List<BaseBankItem>> = _itemsLiveData

    private val _errorLiveData = MutableLiveData<Unit>()
    val errorLiveData: LiveData<Unit> = _errorLiveData

    private val _loadingLiveData = MutableLiveData<Boolean>()
    val loadingLiveData: LiveData<Boolean> = _loadingLiveData

    fun loadData(descriptor: AsyncPaymentMethodDescriptor) = viewModelScope.launch {
        interactor(
            IssuingBankParams(
                descriptor.config.id.orEmpty(),
                descriptor.config.type,
                descriptor.localConfig.settings.locale
            )
        )
            .onStart { _loadingLiveData.postValue(true) }
            .onCompletion { _loadingLiveData.postValue(false) }
            .catch {
                _errorLiveData.postValue(Unit)
            }.collect {
                _itemsLiveData.postValue(it.map { BankItem(it.id, it.name, it.iconUrl) })
            }
    }

    fun onBankSelected(issuerId: String) {
        val currentItems = _itemsLiveData.value.orEmpty()
        val newItems = currentItems.filterIsInstance<BankItem>().map { bankItem ->
            if (bankItem.id == issuerId) {
                bankItem.toLoadingBankItem()
            } else { bankItem.toDisabledBankItem() }
        }
        _itemsLiveData.postValue(newItems)
        logAnalyticsBankSelected(issuerId)
    }

    private fun logAnalyticsBankSelected(issuerId: String) =
        addAnalyticsEvent(
            UIAnalyticsParams(
                AnalyticsAction.CLICK,
                ObjectType.LIST_ITEM,
                Place.BANK_SELECTION_LIST,
                ObjectId.SELECT,
                BankIssuerContextParams(issuerId)
            )
        )
}
