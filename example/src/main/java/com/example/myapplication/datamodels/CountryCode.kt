package com.example.myapplication.datamodels

enum class AppCountryCode(val currencyCode: CurrencyCode, val flag: String) {
    GB(CurrencyCode.GBP, "🇬🇧"),
    BE(CurrencyCode.EUR, "🇧🇪"),
    DE(CurrencyCode.EUR, "🇩🇪"),
    NL(CurrencyCode.EUR, "🇳🇱"),
    SE(CurrencyCode.SEK, "🇸🇪"),
    AU(CurrencyCode.AUD, "🇦🇺"),
    TR(CurrencyCode.TL, "🇹🇷"),
    GE(CurrencyCode.GEL, "🇬🇪"),
    JP(CurrencyCode.JPY, "🇯🇵"),
    KR(CurrencyCode.KRW, "🇰🇷"),
    SG(CurrencyCode.SGD, "🇸🇬"),
    CH(CurrencyCode.CHF, "🇨🇭"),
    CN(CurrencyCode.CNY, "🇨🇳"),
    NO(CurrencyCode.NOK, "🇳🇴"),
    PL(CurrencyCode.PLN, "🇵🇱"),
    DK(CurrencyCode.DKK, "🇩🇰"),
    CA(CurrencyCode.CAD, "🇨🇦"),
    US(CurrencyCode.USD, "🇺🇸"),
    PH(CurrencyCode.PHP, "🇵🇭"),
    TH(CurrencyCode.THB, "🇹🇭");
}
