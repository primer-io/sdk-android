package io.primer.android.configuration.domain

import io.primer.android.configuration.domain.model.Configuration
import io.primer.android.configuration.domain.model.ConfigurationParams
import io.primer.android.configuration.domain.repository.ConfigurationRepository
import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.core.extensions.onError
import io.primer.android.core.logging.internal.LogReporter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Represents the caching policy to be used for network requests.
 */
sealed interface CachePolicy {
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

typealias ConfigurationInteractor = BaseSuspendInteractor<Configuration, ConfigurationParams>

internal class DefaultConfigurationInteractor(
    private val configurationRepository: ConfigurationRepository,
    private val logReporter: LogReporter,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseSuspendInteractor<Configuration, ConfigurationParams>() {
    override suspend fun performAction(params: ConfigurationParams): Result<Configuration> {
        return configurationRepository.fetchConfiguration(cachePolicy = params.cachePolicy).onError { throwable ->
            logReporter.error(CONFIGURATION_ERROR, throwable = throwable)
        }
    }

    private companion object {
        const val CONFIGURATION_ERROR =
            "Failed to initialise due to a failed network call. Please ensure" +
                " your internet connection is stable and try again."
    }
}
