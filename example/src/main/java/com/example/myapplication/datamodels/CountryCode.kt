package com.example.myapplication.datamodels

import io.primer.android.model.dto.CountryCode

enum class AppCountryCode(val currencyCode: CurrencyCode) {
    GB(CurrencyCode.GBP),
    DE(CurrencyCode.EUR),
    SE(CurrencyCode.SEK),
    AU(CurrencyCode.AUD),
    TR(CurrencyCode.TL),
    GE(CurrencyCode.GEL),
    JP(CurrencyCode.JPY),
    KR(CurrencyCode.KRW),
    SG(CurrencyCode.SGD);

    val mapped: CountryCode get() = CountryCode.valueOf(this.name)
}