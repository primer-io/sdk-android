package io.primer.android.domain.session

import io.primer.android.domain.base.BaseInteractor
import io.primer.android.domain.session.models.ConfigurationParams
import io.primer.android.domain.session.repository.ConfigurationRepository
import io.primer.android.events.EventDispatcher
import io.primer.android.extensions.toCheckoutErrorEvent
import io.primer.android.logging.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn

internal class ConfigurationInteractor(
    private val configurationRepository: ConfigurationRepository,
    private val eventDispatcher: EventDispatcher,
    private val logger: Logger,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseInteractor<Unit, ConfigurationParams>() {

    override fun execute(params: ConfigurationParams) =
        configurationRepository.fetchConfiguration(params.fromCache)
            .flowOn(dispatcher)
            .catch {
                eventDispatcher.dispatchEvent(it.toCheckoutErrorEvent(CONFIGURATION_ERROR))
                logger.error(CONFIGURATION_ERROR, it)
            }

    private companion object {

        const val CONFIGURATION_ERROR =
            "Failed to initialise due to a failed network call. Please ensure" +
                "your internet connection is stable and try again."
    }
}
