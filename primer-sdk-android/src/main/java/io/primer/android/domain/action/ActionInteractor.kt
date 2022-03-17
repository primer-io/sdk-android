package io.primer.android.domain.action

import io.primer.android.data.action.models.ClientSessionActionsRequest
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.domain.session.repository.ConfigurationRepository
import io.primer.android.domain.token.ValidateTokenRepository
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.utils.Failure
import io.primer.android.utils.Success
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal class ActionInteractor(
    private val validationTokenRepository: ValidateTokenRepository,
    private val actionRepository: ActionRepository,
    private val configurationRepository: ConfigurationRepository,
    private val clientTokenRepository: ClientTokenRepository,
    private val localConfigurationDataSource: LocalConfigurationDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    val surcharges: Map<String, Int>
        get() = localConfigurationDataSource
            .getConfiguration()
            .clientSession
            ?.paymentMethod
            ?.surcharges
            ?: mapOf()

    val surchargeDataEmptyOrZero: Boolean
        get() = surcharges.all { item -> item.value == 0 } || surcharges.isEmpty()

    fun dispatch(
        request: ClientSessionActionsRequest,
        completion: (error: Error?) -> Unit
    ) = actionRepository.dispatch(request) { result ->
        when (result) {
            is Success -> {
                if (result.value == null) completion(null)
                else {
                    CoroutineScope(dispatcher).launch {
                        validationTokenRepository.validate(result.value).catch { e ->
                            completion(Error(e))
                        }.collect {
                            clientTokenRepository.setClientToken(result.value)
                            fetchConfiguration { error -> completion(error) }
                        }
                    }
                }
            }
            is Failure -> completion(result.value)
        }
    }

    private suspend fun fetchConfiguration(
        completion: (error: Error?) -> Unit,
    ) = configurationRepository
        .fetchConfiguration(false)
        .catch { completion(Error("Failed to fetch configuration")) }
        .collect { completion(null) }
}
