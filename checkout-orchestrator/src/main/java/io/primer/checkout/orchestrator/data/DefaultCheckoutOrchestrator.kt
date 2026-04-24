package io.primer.checkout.orchestrator.data

import android.util.Base64
import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.Severity
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.BdcFlowStartContextParams
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.checkout.orchestrator.ManifestOverrides
import io.primer.checkout.orchestrator.data.datasource.ManifestRemoteDataSource
import io.primer.checkout.orchestrator.data.model.ExecuteInstructionPayload
import io.primer.checkout.orchestrator.data.model.ProcessingResult
import io.primer.checkout.orchestrator.data.model.ResolvedAction
import io.primer.checkout.orchestrator.domain.CheckoutOrchestrator
import io.primer.checkout.orchestrator.domain.SdkContextProvider
import io.primer.checkout.orchestrator.domain.error.CheckoutOrchestratorException
import io.primer.executionengine.domain.models.Outcome
import io.primer.executionengine.domain.models.StepResult
import io.primer.executionengine.domain.registry.StepExecutorRegistry
import io.primer.jscore.domain.core.JsExecutor
import io.primer.jscore.domain.core.models.JsResourceUrl
import kotlinx.coroutines.coroutineScope
import org.json.JSONObject
import java.security.MessageDigest

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

    override suspend fun start(
        paymentMethodType: String,
        payload: String,
    ) = runSuspendCatching {
        logBdcFlowStart()

        val instructionPayload = ExecuteInstructionPayload.deserializer.deserialize(
            JSONObject(payload),
        )

        this.schema = instructionPayload.schema
        val currentState = instructionPayload.parameters

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
            val context = sdkContextProvider.provide(paymentMethodType)
            val resultJson = jsExecutor.initializeStateProcessor(
                schema = schema,
                state = currentState,
                sdkContext = context,
            ).getOrThrow()

            val processingResult = parseProcessingResult(resultJson)
            processResult(result = processingResult, paymentMethodType = paymentMethodType)
        }
    }

    private suspend fun processResult(result: ProcessingResult, paymentMethodType: String): Outcome {
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
                Outcome.SUCCESS, Outcome.CANCELLED -> result.terminal.outcome
                Outcome.ERROR, Outcome.UNSUPPORTED, Outcome.UNKNOWN ->
                    throw CheckoutOrchestratorException.TerminalErrorException()
            }

            else -> throw CheckoutOrchestratorException.MissingActionException()
        }
    }

    private suspend fun executeAction(action: ResolvedAction, paymentMethodType: String): ProcessingResult {
        val stepResult = stepExecutorRegistry.executeAction(
            actionId = action.id,
            type = action.type,
            params = action.params,
        ).getOrElse {
            StepResult(
                outcome = Outcome.ERROR,
                actionId = action.id,
            )
        }

        val resultJson = jsExecutor.applyResult(
            schema = schema,
            state = currentState,
            sdkContext = sdkContextProvider.provide(paymentMethodType),
            actionId = stepResult.actionId,
            outcome = stepResult.outcome.value,
            response = stepResult.data.toString(),
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
}
