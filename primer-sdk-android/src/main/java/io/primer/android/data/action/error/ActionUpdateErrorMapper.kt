package io.primer.android.data.action.error

import io.primer.android.data.error.DefaultErrorMapper
import io.primer.android.http.exception.HttpException
import io.primer.android.domain.error.models.ActionUpdateFailedError
import io.primer.android.domain.error.models.HttpError
import io.primer.android.domain.error.models.PrimerError
import java.net.HttpURLConnection

internal class ActionUpdateErrorMapper : DefaultErrorMapper() {

    override fun getPrimerError(throwable: Throwable): PrimerError {
        if (throwable is HttpException &&
            throwable.errorCode in
            HttpURLConnection.HTTP_BAD_REQUEST until HttpURLConnection.HTTP_INTERNAL_ERROR
        ) {
            return HttpError.HttpClientError(
                throwable.errorCode,
                throwable.error.diagnosticsId,
                throwable.error.description,
                ActionUpdateFailedError(throwable.error.description, throwable.error.diagnosticsId)
            )
        }
        return super.getPrimerError(throwable)
    }
}
