package io.primer.checkout.orchestrator.data.repository

import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.core.logging.internal.LogReporter
import io.primer.checkout.orchestrator.data.ClientCapabilitiesProvider
import io.primer.checkout.orchestrator.data.datasource.ClientRequirementsDataSource
import io.primer.checkout.orchestrator.data.model.ProbeRequest
import io.primer.checkout.orchestrator.domain.SdkContextProvider
import io.primer.checkout.orchestrator.domain.model.ClientRequirementsCheck
import io.primer.checkout.orchestrator.domain.repository.ClientRequirementsRepository
import io.primer.executionengine.domain.probe.ProbeResult
import io.primer.executionengine.domain.registry.ProbeRegistry
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

internal class ClientRequirementsTimeoutException(timeout: Duration) :
    Exception("Client requirements were not settled within $timeout")

/**
 * One check per configuration, shared by every backend-driven method's checker. A failure holds for the
 * configuration too: the checkers run one after another, and each would otherwise wait out a check of its own.
 */
internal class ClientRequirementsDataRepository(
    private val clientRequirementsDataSource: ClientRequirementsDataSource,
    private val configurationDataSource: CacheConfigurationDataSource,
    private val sdkContextProvider: SdkContextProvider,
    private val capabilitiesProvider: ClientCapabilitiesProvider,
    private val probeRegistry: ProbeRegistry,
    private val logReporter: LogReporter,
    private val timeout: Duration = TIMEOUT,
) : ClientRequirementsRepository {
    private val mutex = Mutex()
    private var checked: Pair<ConfigurationData, Result<Map<String, ClientRequirementsCheck>>>? = null

    // Checks in the caller's coroutine; a check cancelled with it leaves nothing behind.
    override suspend fun getVerdicts(): Result<Map<String, ClientRequirementsCheck>> {
        val configuration = configurationDataSource.get()
        return mutex.withLock {
            checked?.takeIf { (checkedFor, _) -> checkedFor === configuration }?.second
                ?: check(configuration).also { checked = configuration to it }
        }
    }

    private suspend fun check(configuration: ConfigurationData): Result<Map<String, ClientRequirementsCheck>> =
        runSuspendCatching {
            val methods = configuration.paymentMethods.filter { it.isBackendDriven }
            if (methods.isEmpty()) return@runSuspendCatching emptyMap()

            logReporter.info("BDC: checking client requirements for ${methods.joinToString { it.summary }}")
            val startedAt = TimeSource.Monotonic.markNow()
            val environment = configuration.environment.environment
            val capabilities = capabilitiesProvider.provide()
            val context = sdkContextProvider.provide(paymentMethodType = null, paymentId = null)
            val verdicts = withTimeoutOrNull(timeout) {
                val requests = clientRequirementsDataSource.probesToRun(environment, methods, capabilities, context)
                clientRequirementsDataSource.check(environment, methods, capabilities, context, runProbes(requests))
            } ?: throw ClientRequirementsTimeoutException(timeout)
            logReporter.info("BDC: client requirements settled in ${startedAt.elapsedNow()}: $verdicts")
            verdicts
        }

    // All at once, each result under its request's key.
    private suspend fun runProbes(requests: List<ProbeRequest>): Map<String, ProbeResult> =
        coroutineScope {
            requests.map { request -> async { request.key to probeRegistry.run(request.probe, request.params) } }
                .awaitAll()
                .toMap()
        }

    private val PaymentMethodConfigDataResponse.summary: String
        get() = "$type ($id): ${clientRequirements ?: "none sent, legacy defaults apply"}"

    private companion object {
        val TIMEOUT = 5.seconds
    }
}
