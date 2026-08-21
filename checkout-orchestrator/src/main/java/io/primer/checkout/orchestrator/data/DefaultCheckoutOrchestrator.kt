package io.primer.checkout.orchestrator.data

import android.util.Base64
import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.Severity
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.BdcFlowStartContextParams
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.core.data.serialization.json.extensions.toJSONObject
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.checkout.orchestrator.ManifestOverrides
import io.primer.checkout.orchestrator.data.datasource.ManifestRemoteDataSource
import io.primer.checkout.orchestrator.data.model.ExecuteInstructionPayload
import io.primer.checkout.orchestrator.data.model.ProcessingResult
import io.primer.checkout.orchestrator.data.model.ResolvedAction
import io.primer.checkout.orchestrator.data.model.SdkOwnedInitialState
import io.primer.checkout.orchestrator.domain.CheckoutOrchestrator
import io.primer.checkout.orchestrator.domain.SdkContextProvider
import io.primer.checkout.orchestrator.domain.error.CheckoutOrchestratorException
import io.primer.checkout.orchestrator.domain.model.CheckoutFlowOutcome
import io.primer.executionengine.domain.models.Outcome
import io.primer.executionengine.domain.models.StepResult
import io.primer.executionengine.domain.registry.StepExecutorRegistry
import io.primer.jscore.domain.core.JsExecutor
import io.primer.jscore.domain.core.models.JsResourceUrl
import io.primer.statetransport.domain.model.ClientInstructions
import io.primer.statetransport.domain.model.CurrentAttempt
import io.primer.statetransport.domain.model.InstructionFetch
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import org.json.JSONObject
import java.security.MessageDigest
import kotlin.time.Duration.Companion.milliseconds

internal class DefaultCheckoutOrchestrator(
    private val jsExecutor: JsExecutor,
    private val stepExecutorRegistry: StepExecutorRegistry,
    private val manifestRemoteDataSource: ManifestRemoteDataSource,
    private val configurationDataSource: CacheConfigurationDataSource,
    private val sdkContextProvider: SdkContextProvider,
    private val analyticsInteractor: AnalyticsInteractor,
    private val trustedPublicKeysB64: List<String>,
) : CheckoutOrchestrator {

    private var schema: String = ""
    private var currentState: String = ""
    private var paymentId: String? = null

    override suspend fun start(
        paymentMethodType: String,
        envelope: InstructionFetch,
    ) = runSuspendCatching {
        try {
            val instruction = envelope.instruction
            require(instruction is ClientInstructions.Execute) {
                "CheckoutOrchestrator requires an EXECUTE instruction."
            }
            paymentId = envelope.currentAttempt?.paymentId
            logBdcFlowStart()

            val instructionPayload = ExecuteInstructionPayload.deserializer.deserialize(
                JSONObject(instruction.payload),
            )

            this.schema = instructionPayload.schema
            currentState = buildInitialState(
                parameters = instructionPayload.parameters,
                currentAttempt = envelope.currentAttempt,
            )

            val manifestUrl = ManifestOverrides.url
                ?: ManifestRemoteDataSource.buildManifestUrl(
                    configurationDataSource.get().environment.environment,
                )
            val manifest = manifestRemoteDataSource.fetchManifest(manifestUrl)

            jsExecutor.initialize(
                listOf(
                    JsResourceUrl(
                        url = manifest.stateProcessor.umd.url,
                        expectedSha256 = manifest.stateProcessor.umd.sha256,
                    ),
                    JsResourceUrl(
                        url = manifest.cel.noModules.wasm.gz,
                        expectedSha256 = manifest.cel.noModules.wasm.sha256,
                    ),
                    JsResourceUrl(
                        url = manifest.cel.noModules.js.url,
                        expectedSha256 = manifest.cel.noModules.js.sha256,
                    ),
                ),
            ).getOrThrow()

            coroutineScope {
                val context = sdkContextProvider.provide(paymentMethodType, paymentId)
                val resultJson = jsExecutor.initializeStateProcessor(
                    schema = schema,
                    state = currentState,
                    sdkContext = context,
                ).getOrThrow()

                val processingResult = parseProcessingResult(resultJson)
                processResult(result = processingResult, paymentMethodType = paymentMethodType)
            }
        } finally {
            // Fires on terminal outcome, error, AND cancellation.
            stepExecutorRegistry.onFinish()
        }
    }

    /**
     * Merges the SDK-owned values into the schema-provided parameters.
     */
    private fun buildInitialState(
        parameters: String,
        currentAttempt: CurrentAttempt?,
    ): String {
        val configurationData = configurationDataSource.get()
        val sdkOwnedState = JSONSerializationUtils.getJsonObjectSerializer<SdkOwnedInitialState>().serialize(
            SdkOwnedInitialState(
                sdk = SdkOwnedInitialState.SdkUrls(
                    pciUrl = configurationData.pciUrl,
                    coreUrl = configurationData.coreUrl,
                ),
                currentAttempt = currentAttempt,
            ),
        )
        return JSONObject(parameters).apply {
            SdkOwnedInitialState.SDK_OWNED_KEYS.forEach(::remove)
            sdkOwnedState.keys().forEach { key -> put(key, sdkOwnedState.get(key)) }
        }.toString()
    }

    private suspend fun processResult(result: ProcessingResult, paymentMethodType: String): CheckoutFlowOutcome {
        currentState = result.newState
        return when {
            result.error != null -> {
                throw CheckoutOrchestratorException.StateProcessorException(
                    code = result.error.code,
                    stateProcessorDiagnosticsId = result.error.diagnosticsId,
                    stateProcessorMessage = result.error.message,
                )
            }

            result.action != null -> {
                val nextResult = executeAction(action = result.action, paymentMethodType = paymentMethodType)
                processResult(result = nextResult, paymentMethodType = paymentMethodType)
            }

            result.terminal != null -> when (result.terminal.outcome) {
                Outcome.SUCCESS -> CheckoutFlowOutcome.Completed
                Outcome.CANCELLED -> CheckoutFlowOutcome.Cancelled
                Outcome.ERROR, Outcome.UNSUPPORTED, Outcome.UNKNOWN ->
                    throw CheckoutOrchestratorException.TerminalErrorException()
            }

            // No action, no terminal, no error: the flow is pending, awaiting backend
            // progress; the instruction loop keeps polling until the backend END.
            else -> CheckoutFlowOutcome.Pending
        }
    }

    private suspend fun executeAction(action: ResolvedAction, paymentMethodType: String): ProcessingResult {
        // Pacing: an aborted wait propagates CancellationException (teardown), never a step ERROR.
        action.delayMs?.takeIf { it > 0 }?.let { delay(it.milliseconds) }

        val stepResult = stepExecutorRegistry.executeAction(
            actionId = action.id,
            type = action.type,
            params = action.params,
        ).getOrElse {
            StepResult(
                outcome = Outcome.ERROR,
                actionId = action.id,
                data = mapOf(MESSAGE_FIELD to it.message),
            )
        }

        val resultJson = jsExecutor.applyResult(
            schema = schema,
            state = currentState,
            sdkContext = sdkContextProvider.provide(paymentMethodType, paymentId),
            actionId = stepResult.actionId,
            outcome = stepResult.outcome.value,
            response = stepResult.data.toJSONObject().toString(),
        ).getOrThrow()

        return parseProcessingResult(resultJson)
    }

    private fun parseProcessingResult(json: String): ProcessingResult {
        return JSONSerializationUtils.getJsonObjectDeserializer<ProcessingResult>().deserialize(JSONObject(json))
    }

    private suspend fun logBdcFlowStart() {
        val fingerprints = trustedPublicKeysB64.map { keyB64 ->
            val keyBytes = Base64.decode(keyB64, Base64.DEFAULT)
            val digest = MessageDigest.getInstance("SHA-256").digest(keyBytes)
            digest.joinToString("") { "%02x".format(it) }
        }
        analyticsInteractor(
            MessageAnalyticsParams(
                messageType = MessageType.BDC_FLOW_START,
                message = "BDC flow started.",
                severity = Severity.INFO,
                context = BdcFlowStartContextParams(trustedKeyFingerprints = fingerprints),
            ),
        )
    }

    private companion object {
        const val MESSAGE_FIELD = "message"
    }
}
