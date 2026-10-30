package io.primer.checkout.orchestrator.data.datasource

import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.core.data.serialization.json.extensions.toJSONObject
import io.primer.android.core.data.serialization.json.extensions.toMap
import io.primer.checkout.orchestrator.data.StateProcessorLoader
import io.primer.checkout.orchestrator.data.model.ClientCapabilities
import io.primer.checkout.orchestrator.data.model.ProbeRequest
import io.primer.checkout.orchestrator.domain.model.ClientRequirementsCheck
import io.primer.executionengine.domain.probe.ProbeResult
import org.json.JSONArray
import org.json.JSONObject

/** The state processor's client requirements check, to and from JSON. */
internal class ClientRequirementsDataSource(
    private val stateProcessorLoader: StateProcessorLoader,
) {
    /** The probes the methods' requirements ask for. */
    suspend fun probesToRun(
        environment: String,
        methods: List<PaymentMethodConfigDataResponse>,
        capabilities: ClientCapabilities,
        context: String,
    ): List<ProbeRequest> {
        val requests = stateProcessorLoader.execute(environment) {
            probesToRun(items = itemsOf(methods), client = clientOf(capabilities, context))
        }
        return probeRequestsOf(requests)
    }

    /** Verdicts by type, given the probes' [results] by request key. */
    suspend fun check(
        environment: String,
        methods: List<PaymentMethodConfigDataResponse>,
        capabilities: ClientCapabilities,
        context: String,
        results: Map<String, ProbeResult>,
    ): Map<String, ClientRequirementsCheck> {
        val verdicts = stateProcessorLoader.execute(environment) {
            checkClientRequirements(
                items = itemsOf(methods),
                client = clientOf(capabilities, context),
                results = resultsOf(results),
            )
        }
        return verdictsOf(verdicts)
    }

    private fun probeRequestsOf(json: String): List<ProbeRequest> {
        val requests = JSONArray(json)
        return (0 until requests.length()).map { index ->
            val request = requests.getJSONObject(index)
            ProbeRequest(
                key = request.getString(KEY_FIELD),
                probe = request.getString(PROBE_FIELD),
                params = request.optJSONObject(PARAMS_FIELD)?.toMap().orEmpty(),
            )
        }
    }

    private fun resultsOf(results: Map<String, ProbeResult>): String =
        JSONObject().apply {
            results.forEach { (key, result) ->
                put(
                    key,
                    JSONObject().apply {
                        put(OUTCOME_FIELD, result.outcome.value)
                        if (result.data.isNotEmpty()) put(DATA_FIELD, result.data.toJSONObject())
                    },
                )
            }
        }.toString()

    private fun itemsOf(methods: List<PaymentMethodConfigDataResponse>): String =
        JSONArray(
            methods.map { method ->
                JSONObject().apply {
                    putOpt(ID_FIELD, method.id)
                    put(TYPE_FIELD, method.type)
                    method.clientRequirements?.let { put(CLIENT_REQUIREMENTS_FIELD, JSONObject(it)) }
                }
            },
        ).toString()

    private fun clientOf(capabilities: ClientCapabilities, context: String): String =
        JSONObject().apply {
            put(
                CAPABILITIES_FIELD,
                JSONObject().apply {
                    put(STEP_TYPES_FIELD, capabilities.stepTypes.toJSONObject())
                    put(UI_COMPONENTS_FIELD, capabilities.uiComponents.toJSONObject())
                    put(PROBES_FIELD, capabilities.probes.toJSONObject())
                },
            )
            put(CONTEXT_FIELD, JSONObject(context))
        }.toString()

    private fun verdictsOf(json: String): Map<String, ClientRequirementsCheck> {
        val verdicts = JSONObject(json)
        return verdicts.keys().asSequence().associateWith { type ->
            val verdict = verdicts.getJSONObject(type)
            ClientRequirementsCheck(
                satisfied = verdict.getBoolean(SATISFIED_FIELD),
                unmet = verdict.optJSONArray(UNMET_FIELD)?.toString() ?: EMPTY_UNMET,
            )
        }
    }

    private companion object {
        const val ID_FIELD = "id"
        const val TYPE_FIELD = "type"
        const val CLIENT_REQUIREMENTS_FIELD = "clientRequirements"
        const val CAPABILITIES_FIELD = "capabilities"
        const val STEP_TYPES_FIELD = "stepTypes"
        const val UI_COMPONENTS_FIELD = "uiComponents"
        const val PROBES_FIELD = "probes"
        const val KEY_FIELD = "key"
        const val PROBE_FIELD = "probe"
        const val PARAMS_FIELD = "params"
        const val OUTCOME_FIELD = "outcome"
        const val DATA_FIELD = "data"
        const val CONTEXT_FIELD = "context"
        const val SATISFIED_FIELD = "satisfied"
        const val UNMET_FIELD = "unmet"
        const val EMPTY_UNMET = "[]"
    }
}
