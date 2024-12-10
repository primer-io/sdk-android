package io.primer.android.domain.helper

import android.content.Context
import android.util.Log
import io.primer.android.clientSessionActions.domain.models.PrimerCountriesCodeInfo
import io.primer.android.clientSessionActions.domain.models.PrimerCountry
import io.primer.android.clientSessionActions.domain.models.PrimerPhoneCode
import io.primer.android.R
import io.primer.android.configuration.data.model.CountryCode
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.core.data.serialization.json.extensions.sequence
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

internal class CountriesDataRepository(private val context: Context) :
    CountriesRepository {

    private val countries = mutableListOf<PrimerCountry>()
    private val phoneCodes = mutableListOf<PrimerPhoneCode>()

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
                error("Can't to fetch data from json RAW folder")
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
        val countryCodesData =
            JSONSerializationUtils.getJsonObjectDeserializer<PrimerCountriesCodeInfo>()
                .deserialize(JSONObject(dataJson))
        countries.clear()
        countries.addAll(
            countryCodesData.countries.entries.map { entry ->
                if (entry.value is ArrayList<*>) {
                    PrimerCountry(
                        (entry.value as ArrayList<*>).firstOrNull()?.toString() ?: "N/A",
                        enumValueOf(entry.key)
                    )
                } else {
                    PrimerCountry(entry.value.toString(), enumValueOf(entry.key))
                }
            }
        )
    }

    private fun loadPhoneCodes(fromCache: Boolean = false) {
        if (!fromCache || phoneCodes.isEmpty()) {
            val dataJson = context.resources?.openRawResource(R.raw.phone_number_country_codes)
                ?.readBytes()
                ?.decodeToString().orEmpty()
            if (dataJson.isNotBlank()) {
                try {
                    val phoneCodesData = JSONArray(dataJson).sequence<JSONObject>().map {
                        JSONSerializationUtils
                            .getJsonObjectDeserializer<PrimerPhoneCode>().deserialize(it)
                    }.toList()
                    phoneCodes.clear()
                    phoneCodes.addAll(phoneCodesData)
                } catch (e: IOException) {
                    Log.e("Primer", e.message.toString())
                }
            } else {
                error("Can't to fetch data from json RAW folder")
            }
        } else {
            // phone codes is loaded, no need to reload
        }
    }

    override suspend fun getCountries(): List<PrimerCountry> {
        loadCountries()
        return countries.toList()
    }

    override suspend fun getCountryByCode(code: CountryCode): PrimerCountry {
        loadCountries()
        return countries.find { it.code == code } ?: PrimerCountry.default
    }

    override suspend fun findCountryByQuery(query: String): List<PrimerCountry> {
        loadCountries(fromCache = true)
        val queryTrimmed = query.trim()
        return countries.filter {
            it.name.contains(queryTrimmed, ignoreCase = true) ||
                it.code.name.contentEquals(queryTrimmed, ignoreCase = true)
        }.toList()
    }

    override fun getPhoneCodes(): List<PrimerPhoneCode> {
        loadPhoneCodes()
        return phoneCodes.toList()
    }

    override fun getPhoneCodeByCountryCode(code: CountryCode): PrimerPhoneCode {
        loadPhoneCodes()
        return phoneCodes.find { it.code == code } ?: PrimerPhoneCode.default
    }

    override fun findPhoneCodeByQuery(query: String): List<PrimerPhoneCode> {
        loadPhoneCodes(fromCache = true)
        val queryTrimmed = query.trim()
        return phoneCodes.filter {
            it.dialCode.contains(queryTrimmed, ignoreCase = true) ||
                it.code.name.contains(queryTrimmed, ignoreCase = true) ||
                it.name.contains(queryTrimmed, ignoreCase = true)
        }.toList()
    }
}
