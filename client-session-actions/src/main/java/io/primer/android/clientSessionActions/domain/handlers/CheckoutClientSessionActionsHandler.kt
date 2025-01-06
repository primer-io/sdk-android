package io.primer.android.clientSessionActions.domain.handlers

import io.primer.android.domain.action.models.PrimerClientSession
import io.primer.android.domain.error.models.PrimerError

interface CheckoutClientSessionActionsHandler {
    fun onClientSessionUpdateStarted()

    fun onClientSessionUpdateSuccess(clientSession: PrimerClientSession)

    fun onClientSessionUpdateError(error: PrimerError)
}
