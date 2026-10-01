package io.primer.checkout.orchestrator.data.datasource

import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.checkout.orchestrator.data.StateProcessorLoader
import io.primer.checkout.orchestrator.data.model.ClientCapabilities
import io.primer.checkout.orchestrator.domain.model.ClientRequirementsCheck
import org.json.JSONArray
import org.json.JSONObject

/** The state processor's client requirements check, to and from JSON. */
internal class ClientRequirementsDataSource(
    private val stateProcessorLoader: StateProcessorLoader,
) {
    /** Verdicts by type, for an SDK with [capabilities] in the checkout's [context]. */
    suspend fun check(
        environment: String,
        methods: List<PaymentMethodConfigDataResponse>,
        capabilities: ClientCapabilities,
        context: String,
    ): Map<String, ClientRequirementsCheck> {
        val verdicts = stateProcessorLoader.execute(environment) {
            checkClientRequirements(items = itemsOf(methods), client = clientOf(capabilities, context))
        }
        return verdictsOf(verdicts)
    }

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
                    put(STEP_TYPES_FIELD, JSONObject(capabilities.stepTypes))
                    put(UI_COMPONENTS_FIELD, JSONObject(capabilities.uiComponents))
                    put(DEPENDENCIES_FIELD, JSONObject(capabilities.dependencies))
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
        const val DEPENDENCIES_FIELD = "dependencies"
        const val CONTEXT_FIELD = "context"
        const val SATISFIED_FIELD = "satisfied"
        const val UNMET_FIELD = "unmet"
        const val EMPTY_UNMET = "[]"
    }
}
