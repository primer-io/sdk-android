package io.primer.android.domain.payments.apaya.models

import android.net.Uri

internal data class ApayaWebResultParams(
    val mxNumber: String,
    val mcc: String,
    val mnc: String,
    val hashedIdentifier: String,
    val success: String,
    val status: String,
) {

    constructor(uri: Uri) : this(
        uri.getQueryParameter(MX_QUERY_PARAM).orEmpty(),
        uri.getQueryParameter(MCC_QUERY_PARAM).orEmpty(),
        uri.getQueryParameter(MNC_QUERY_PARAM).orEmpty(),
        uri.getQueryParameter(HASHED_ID_QUERY_PARAM).orEmpty(),
        uri.getQueryParameter(SUCCESS_QUERY_PARAM).orEmpty(),
        uri.getQueryParameter(STATUS_QUERY_PARAM).orEmpty(),
    )

    private companion object {

        const val MX_QUERY_PARAM = "MX"
        const val MCC_QUERY_PARAM = "MCC"
        const val MNC_QUERY_PARAM = "MNC"
        const val HASHED_ID_QUERY_PARAM = "HashedIdentifier"
        const val SUCCESS_QUERY_PARAM = "success"
        const val STATUS_QUERY_PARAM = "status"
    }
}
