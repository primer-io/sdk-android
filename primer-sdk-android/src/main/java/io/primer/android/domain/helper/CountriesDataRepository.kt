package io.primer.android.domain.helper

import android.content.Context
import io.primer.android.R
import io.primer.android.data.configuration.models.CountryCode
import io.primer.android.domain.action.models.PrimerCountriesCodeInfo
import io.primer.android.domain.action.models.PrimerCountry
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONTokener
import java.lang.ref.WeakReference

class CountriesDataRepository(private val contextRef: WeakReference<Context>) :
    CountriesRepository {

    private val countries = mutableListOf<PrimerCountry>()

    private suspend fun loadCountries(fromCache: Boolean = false) {
        if (!fromCache || countries.isEmpty()) {
            val dataJson = contextRef.get()
                ?.resources?.openRawResource(R.raw.codes_countries)
                ?.readBytes()
                ?.decodeToString().orEmpty()
            if (dataJson.isNotBlank()) {
                val countryCodesData: PrimerCountriesCodeInfo = Json.decodeFromString(dataJson)
                countries.clear()
                try {
                    countries.addAll(
                        countryCodesData.countries.entries.map { entry ->
                            val tokenize = JSONTokener(entry.value.toString())
                            val valueJson = tokenize.nextValue()
                            if (valueJson is JSONArray) {
                                val name = if (valueJson.length() == 0) "N/A"
                                else valueJson.optString(valueJson.length() - 1, "N/A")
                                PrimerCountry(name, enumValueOf(entry.key))
                            } else {
                                PrimerCountry(valueJson.toString(), enumValueOf(entry.key))
                            }
                        }
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                // can't to fetch data from json RAW folder
            }
        } else {
            // countries is loaded, no need to reload
        }
    }

    override suspend fun getCountries(): List<PrimerCountry> {
        loadCountries(fromCache = true)
        return countries.toList()
    }

    override suspend fun getCountryByCode(code: CountryCode): PrimerCountry {
        loadCountries(fromCache = true)
        return countries.find { it.code == code } ?: PrimerCountry.default
    }

    override suspend fun findCountryByQuery(query: String): List<PrimerCountry> {
        loadCountries()
        return countries.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.code.name.contentEquals(query, ignoreCase = true)
        }.toList()
    }
}
