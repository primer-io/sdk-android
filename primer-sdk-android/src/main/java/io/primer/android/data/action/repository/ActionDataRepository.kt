package io.primer.android.data.action.repository

import io.primer.android.completion.DefaultActionResumeHandler
import io.primer.android.data.action.models.ClientSessionActionsRequest
import io.primer.android.domain.action.ActionRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.utils.Either
import io.primer.android.utils.Failure
import io.primer.android.utils.Success

internal class ActionDataRepository(
    private val eventDispatcher: EventDispatcher
) : ActionRepository {

    override fun dispatch(
        request: ClientSessionActionsRequest,
        completion: (Either<String?, Error>) -> Unit
    ) {
        val resumeHandler = DefaultActionResumeHandler { clientToken, error ->
            if (error == null) completion(Success(clientToken))
            else completion(Failure(error))
        }
        val event = CheckoutEvent.OnClientSessionActions(request, resumeHandler)
        eventDispatcher.dispatchEvent(event)
    }
}
