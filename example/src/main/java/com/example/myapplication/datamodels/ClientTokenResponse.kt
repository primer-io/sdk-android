package com.example.myapplication.datamodels

import androidx.annotation.Keep

@Keep
data class ClientTokenResponse(
    val clientToken: String,
    val expirationDate: String,
)
