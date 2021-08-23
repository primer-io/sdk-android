package io.primer.android.data.exception

import io.primer.android.model.dto.APIError

internal class HttpException(val errorCode: Int, val error: APIError) : Exception(error.description)
