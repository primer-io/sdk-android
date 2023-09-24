package io.primer.android.components.data.payments.paymentMethods.nolpay.exception

import io.primer.android.data.base.exceptions.IllegalValueKey

internal enum class NolPayIllegalValueKey(override val key: String) : IllegalValueKey {
    MERCHANT_APP_ID("configuration.merchantAppId"),
    COLLECTED_DATA("collectedData"),
    COLLECTED_DATA_MOBILE_NUMBER("collectedData.mobileNumber"),
    COLLECTED_DATA_DIALLING_CODE("collectedData.phoneCountryDiallingCode"),
    SAVED_DATA_LINK_TOKEN("linkToken");
}
