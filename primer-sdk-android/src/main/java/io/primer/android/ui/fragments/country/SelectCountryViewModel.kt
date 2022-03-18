package io.primer.android.ui.fragments.country

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.domain.helper.CountriesRepository
import io.primer.android.model.dto.Country
import io.primer.android.model.dto.CountryCode
import io.primer.android.model.dto.emojiFlag
import io.primer.android.presentation.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class SelectCountryViewModel(
    private val countriesRepository: CountriesRepository,
    analyticsInteractor: AnalyticsInteractor
) : BaseViewModel(analyticsInteractor) {

    private val _countriesData = MutableLiveData<List<CountryItem>>()
    val countriesData: LiveData<List<CountryItem>> = _countriesData

    fun fetchCountries() {
        viewModelScope.launch(Dispatchers.IO) {
            val countries = countriesRepository.getCountries()
            _countriesData.postValue(countries.map { CountryItem(it.name, it.code, it.code.emojiFlag()) })
        }
    }

    fun onFilterChanged(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val countries = if (query.isBlank()) countriesRepository.getCountries()
            else countriesRepository.findCountryByQuery(query)
            _countriesData.postValue(countries.map { CountryItem(it.name, it.code, it.code.emojiFlag()) })
        }
    }

    fun getCountryByCode(code: CountryCode, onComplete: (Country) -> Unit) {
        viewModelScope.launch {
            val country = countriesRepository.getCountryByCode(code)
            withContext(Dispatchers.Main) {
                onComplete(country)
            }
        }
    }
}
