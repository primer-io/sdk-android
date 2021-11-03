package io.primer.android.data.exception

import io.primer.android.model.dto.APIError

internal class HttpException(errorCode: Int, val error: APIError) :
    Exception(
        listOf(
            "Error code: $errorCode",
            "Description: ${error.description}",
            "DiagnosticsId: ${error.diagnosticsId.orEmpty()}",
            "Validation errors: ${error.validationErrors.map { it.model + it.errors }}",
        ).joinToString(", ")
    )
