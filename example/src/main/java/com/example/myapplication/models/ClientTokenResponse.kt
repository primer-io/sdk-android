package com.example.myapplication.models

import androidx.annotation.Keep

@Keep
data class ClientTokenResponse(
    val clientToken: String,
    val expirationDate: String,
)
