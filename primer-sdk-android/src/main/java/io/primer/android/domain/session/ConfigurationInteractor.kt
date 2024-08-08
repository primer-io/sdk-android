package io.primer.android.domain.session

import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.session.models.Configuration
import io.primer.android.domain.session.models.ConfigurationParams
import io.primer.android.domain.session.repository.ConfigurationRepository
import io.primer.android.core.logging.internal.LogReporter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn

/**
 * Represents the caching policy to be used for network requests.
 */
internal sealed interface CachePolicy {

    /**
     * Always use the cached data, even if it is stale.
     */
    data object ForceCache : CachePolicy

    /**
     * Use the cached data if it is available, otherwise fall back to a network request.
     */
    data object CacheFirst : CachePolicy

    /**
     * Always make a network request, ignoring any cached data.
     */
    data object ForceNetwork : CachePolicy
}

internal class ConfigurationInteractor(
    private val configurationRepository: ConfigurationRepository,
    private val baseErrorEventResolver: BaseErrorEventResolver,
    private val logReporter: LogReporter,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseFlowInteractor<Configuration, ConfigurationParams>() {

    override fun execute(params: ConfigurationParams) =
        configurationRepository.fetchConfiguration(params.cachePolicy)
            .flowOn(dispatcher)
            .catch { throwable ->
                baseErrorEventResolver.resolve(throwable, ErrorMapperType.DEFAULT)
                logReporter.error(CONFIGURATION_ERROR, throwable = throwable)
            }

    private companion object {

        const val CONFIGURATION_ERROR =
            "Failed to initialise due to a failed network call. Please ensure" +
                " your internet connection is stable and try again."
    }
}
