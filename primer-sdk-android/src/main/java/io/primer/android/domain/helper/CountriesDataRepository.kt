package io.primer.android.domain.helper

import android.content.Context
import android.util.Log
import io.primer.android.R
import io.primer.android.data.configuration.models.CountryCode
import io.primer.android.domain.action.models.PrimerCountriesCodeInfo
import io.primer.android.domain.action.models.PrimerCountry
import io.primer.android.model.Serialization
import kotlinx.serialization.decodeFromString
import org.json.JSONArray
import org.json.JSONTokener
import java.io.IOException

class CountriesDataRepository(private val context: Context) :
    CountriesRepository {

    private val countries = mutableListOf<PrimerCountry>()

    /**
     * Use function as suspend for load in background, because work with file
     * and load big string from file in json format.
     */
    private suspend fun loadCountries(fromCache: Boolean = false) {
        if (!fromCache || countries.isEmpty()) {
            val dataJson = context.resources?.openRawResource(R.raw.codes_countries)
                ?.readBytes()
                ?.decodeToString().orEmpty()
            if (dataJson.isNotBlank()) {
                try {
                    parseFileAndLoadCountries(dataJson)
                } catch (e: IOException) {
                    Log.e("Primer", e.message.toString())
                }
            } else {
                throw IllegalStateException("Can't to fetch data from json RAW folder")
            }
        } else {
            // countries is loaded, no need to reload
        }
    }

    /**
     * After load json from file as string, need to serialize and prepare correct model
     * of countries with correct country code and one of types name, because in file
     * contain some countries short of long version of name.
     */
    @Throws(IOException::class)
    private fun parseFileAndLoadCountries(dataJson: String) {
        val countryCodesData = Serialization.json
            .decodeFromString<PrimerCountriesCodeInfo>(dataJson)
        countries.clear()
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
        val queryTrimmed = query.trim()
        return countries.filter {
            it.name.contains(queryTrimmed, ignoreCase = true) ||
                it.code.name.contentEquals(queryTrimmed, ignoreCase = true)
        }.toList()
    }
}
