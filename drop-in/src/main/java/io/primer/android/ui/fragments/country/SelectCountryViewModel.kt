package io.primer.android.ui.fragments.country

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.clientSessionActions.domain.models.PrimerCountry
import io.primer.android.clientSessionActions.domain.models.PrimerPhoneCode
import io.primer.android.configuration.data.model.CountryCode
import io.primer.android.domain.helper.CountriesRepository
import io.primer.android.domain.helper.mapCountryToCountryItem
import io.primer.android.domain.helper.mapPhoneCodesToCountryItem
import io.primer.android.presentation.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class SelectCountryViewModel(
    private val countriesRepository: CountriesRepository,
    analyticsInteractor: AnalyticsInteractor,
) : BaseViewModel(analyticsInteractor) {
    private val _countriesData = MutableLiveData<List<CountryCodeItem>>()
    val countriesData: LiveData<List<CountryCodeItem>> = _countriesData

    fun fetchCountriesData(type: CountryDataType) {
        viewModelScope.launch(Dispatchers.IO) {
            val countriesData =
                when (type) {
                    CountryDataType.NAME ->
                        countriesRepository.getCountries()
                            .mapCountryToCountryItem()
                    CountryDataType.DIAL_CODE ->
                        countriesRepository.getPhoneCodes()
                            .mapPhoneCodesToCountryItem()
                }

            _countriesData.postValue(countriesData)
        }
    }

    fun onFilterChanged(
        type: CountryDataType,
        query: String,
    ) {
        if (query.isBlank()) {
            fetchCountriesData(type)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val countries =
                when (type) {
                    CountryDataType.NAME ->
                        countriesRepository.findCountryByQuery(query)
                            .mapCountryToCountryItem()
                    CountryDataType.DIAL_CODE ->
                        countriesRepository.findPhoneCodeByQuery(query)
                            .mapPhoneCodesToCountryItem()
                }
            _countriesData.postValue(countries)
        }
    }

    fun getCountryByCode(
        code: CountryCode,
        onComplete: (PrimerCountry) -> Unit,
    ) {
        viewModelScope.launch {
            val country = countriesRepository.getCountryByCode(code)
            withContext(Dispatchers.Main) {
                onComplete(country)
            }
        }
    }

    fun getPhoneCodeByCountryCode(
        code: CountryCode,
        onComplete: (PrimerPhoneCode) -> Unit,
    ) {
        viewModelScope.launch {
            val phoneCode = countriesRepository.getPhoneCodeByCountryCode(code)
            withContext(Dispatchers.Main) {
                onComplete(phoneCode)
            }
        }
    }
}
