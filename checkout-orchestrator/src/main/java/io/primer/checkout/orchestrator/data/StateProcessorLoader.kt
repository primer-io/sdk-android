package io.primer.checkout.orchestrator.data

import io.primer.checkout.orchestrator.data.datasource.ManifestRemoteDataSource
import io.primer.checkout.orchestrator.data.model.jsResources
import io.primer.jscore.domain.core.JsExecutor
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** The session's state processor, loaded once and shared by the requirements check and every flow. */
internal class StateProcessorLoader(
    private val manifestRemoteDataSource: ManifestRemoteDataSource,
    private val jsExecutor: JsExecutor,
) {
    private val mutex = Mutex()

    /**
     * Runs [call] on the loaded state processor. Calls are stateless, so one that failed because the engine died,
     * e.g. killed while the app was in the background, runs again on a new load.
     */
    suspend fun <T> execute(environment: String, call: suspend JsExecutor.() -> Result<T>): T {
        val result = load(environment).call()
        return if (result.isFailure && !jsExecutor.isAlive) {
            load(environment).call().getOrThrow()
        } else {
            result.getOrThrow()
        }
    }

    /**
     * Loads in the caller's coroutine; later callers wait for it, and load again when it failed, was cancelled or
     * the engine died. Not keyed by [environment], since a session has only one.
     */
    suspend fun load(environment: String): JsExecutor = mutex.withLock {
        if (!jsExecutor.isAlive) {
            val manifest = manifestRemoteDataSource.fetchManifest(ManifestRemoteDataSource.manifestUrl(environment))
            jsExecutor.initialize(manifest.jsResources).getOrThrow()
        }
        jsExecutor
    }
}
