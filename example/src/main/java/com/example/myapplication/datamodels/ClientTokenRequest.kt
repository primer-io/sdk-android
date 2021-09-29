package com.example.myapplication.datamodels

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ClientTokenRequest(
    @SerializedName("customerId") val id: String,
    @SerializedName("environment") val environment: String,
    @SerializedName("customerCountryCode") val countryCode: String,
) : ExampleAppRequestBody

interface ExampleAppRequestBody {}