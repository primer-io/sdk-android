package io.primer.android.domain.action

import io.primer.android.data.action.models.ClientSessionActionsRequest
import io.primer.android.utils.Either
import java.lang.Error

internal interface ActionRepository {

    fun dispatch(
        request: ClientSessionActionsRequest,
        completion: (Either<String?, Error>) -> Unit
    )
}
