package io.primer.checkout.orchestrator.domain

import android.util.Base64
import io.primer.android.configuration.data.model.Environment
import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.core.domain.None
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.core.utils.BaseDataProvider
import io.primer.checkout.orchestrator.data.StateProcessorLoader
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.json.JSONObject

/** Loads the client token's state processor alongside the configuration request, ready for the requirements check. */
class PreloadStateProcessorInteractor internal constructor(
    private val stateProcessorLoader: StateProcessorLoader,
    private val clientTokenProvider: BaseDataProvider<String>,
    private val logReporter: LogReporter,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseSuspendInteractor<Unit, None>() {

    override suspend fun performAction(params: None): Result<Unit> {
        val environment = environmentOf(clientTokenProvider.provide()) ?: return Result.success(Unit)
        logReporter.info("BDC: preloading state processor for $environment")
        return runSuspendCatching { stateProcessorLoader.load(environment) }
            .onFailure { logReporter.debug("BDC: state processor preload failed: ${it.message}") }
            .map { }
    }

    // Only picks the manifest, whose signature is checked; the token itself is validated elsewhere.
    private fun environmentOf(clientToken: String): String? =
        runCatching {
            val payload = String(Base64.decode(clientToken.split('.')[1], JWT_BASE64_FLAGS))
            val env = JSONObject(payload).getString(ENV_FIELD)
            Environment.entries.firstOrNull { it.name == env }?.environment
        }.getOrNull()

    private companion object {
        const val ENV_FIELD = "env"
        const val JWT_BASE64_FLAGS = Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
    }
}
