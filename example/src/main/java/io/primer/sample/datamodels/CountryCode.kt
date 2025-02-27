package io.primer.sample.datamodels

enum class AppCountryCode(val currencyCode: CurrencyCode, val flag: String) {
    US(CurrencyCode.USD, "🇺🇸"),
    PT(CurrencyCode.EUR, "🇵🇹"),
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
    PH(CurrencyCode.PHP, "🇵🇭"),
    ID(CurrencyCode.IDR, "🇮🇩"),
    MY(CurrencyCode.MYR, "🇲🇾"),
    TH(CurrencyCode.THB, "🇹🇭"),
    AE(CurrencyCode.AED, "🇦🇪"),
    RO(CurrencyCode.RON, "🇷🇴");
}
