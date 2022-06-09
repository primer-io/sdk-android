package io.primer.android.http.exception

import io.primer.android.data.error.model.APIError
import java.net.HttpURLConnection

internal class HttpException(val errorCode: Int, val error: APIError) :
    Exception(
        listOf(
            "Error code: $errorCode",
            "Description: ${error.description}",
            "DiagnosticsId: ${error.diagnosticsId.orEmpty()}",
            "Validation errors: ${error.validationErrors.map { it.model + it.errors }}",
        ).joinToString(", ")
    ) {

    fun isUnAuthorizedError() = errorCode == HttpURLConnection.HTTP_UNAUTHORIZED

    fun isClientError() = errorCode in
        HttpURLConnection.HTTP_BAD_REQUEST until HttpURLConnection.HTTP_INTERNAL_ERROR

    fun isServerError() = errorCode >= HttpURLConnection.HTTP_INTERNAL_ERROR
}
