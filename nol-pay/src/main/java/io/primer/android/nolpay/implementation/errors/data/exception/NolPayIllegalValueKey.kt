package io.primer.android.nolpay.implementation.errors.data.exception

import io.primer.android.errors.data.exception.IllegalValueKey

internal enum class NolPayIllegalValueKey(override val key: String) :
    IllegalValueKey {
    MERCHANT_APP_ID("configuration.merchantAppId"),
    COLLECTED_DATA("collectedData"),
    TRANSACTION_NUMBER("resumeToken.transactionNo"),
    COMPLETE_URL("resumeToken.completeUrl"),
    STATUS_URL("resumeToken.statusUrl"),
    SAVED_DATA_LINK_TOKEN("linkToken"),
}
