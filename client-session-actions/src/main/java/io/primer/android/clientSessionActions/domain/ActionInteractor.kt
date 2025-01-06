package io.primer.android.clientSessionActions.domain

import io.primer.android.clientSessionActions.domain.handlers.CheckoutClientSessionActionsHandler
import io.primer.android.clientSessionActions.domain.models.MultipleActionUpdateParams
import io.primer.android.clientSessionActions.domain.repository.ActionRepository
import io.primer.android.clientSessionActions.domain.validator.ActionUpdateFilter
import io.primer.android.configuration.domain.model.ClientSessionData
import io.primer.android.configuration.domain.repository.ConfigurationRepository
import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.errors.domain.BaseErrorResolver
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

typealias ActionInteractor = BaseSuspendInteractor<ClientSessionData, MultipleActionUpdateParams>

internal class DefaultActionInteractor(
    private val actionRepository: ActionRepository,
    private val configurationRepository: ConfigurationRepository,
    private val actionUpdateFilter: ActionUpdateFilter,
    private val errorEventResolver: BaseErrorResolver,
    private val clientSessionActionsHandler: CheckoutClientSessionActionsHandler,
    private val ignoreErrors: Boolean = false,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseSuspendInteractor<ClientSessionData, MultipleActionUpdateParams>() {
    private var lastParams: MultipleActionUpdateParams? = null

    override suspend fun performAction(params: MultipleActionUpdateParams): Result<ClientSessionData> {
        val filteredActions =
            params.params.filterNot { baseActionUpdateParams -> actionUpdateFilter.filter(baseActionUpdateParams) }

        return when (filteredActions.isNotEmpty() && lastParams != params) {
            true -> {
                lastParams = params
                try {
                    clientSessionActionsHandler.onClientSessionUpdateStarted()
                    actionRepository.updateClientActions(
                        filteredActions,
                    ).onSuccess { clientSessionData ->
                        clientSessionActionsHandler.onClientSessionUpdateSuccess(
                            clientSession = clientSessionData.clientSession,
                        )
                    }
                } catch (e: CancellationException) {
                    /*
                    Clear value to allow calling the action interactor again if the previous call got cancelled due to
                    fast user input
                     */
                    lastParams = null
                    throw e
                }
            }

            false ->
                runSuspendCatching {
                    configurationRepository.getConfiguration()
                        .clientSession.clientSessionDataResponse.toClientSessionData()
                }
        }.onFailure { throwable ->
            if (!ignoreErrors) {
                clientSessionActionsHandler.onClientSessionUpdateError(error = errorEventResolver.resolve(throwable))
            }
        }
    }

    private companion object {
        val TAG = DefaultActionInteractor::class.simpleName
    }
}
