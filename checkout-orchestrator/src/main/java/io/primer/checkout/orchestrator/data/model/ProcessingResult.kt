package io.primer.checkout.orchestrator.data.model

import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.extensions.optNullableLong
import io.primer.executionengine.domain.models.Outcome

/**
 * The next action the SDK must execute.
 */
data class ResolvedAction(
    val id: String,
    val type: String,
    val params: String,
    val delayMs: Long? = null,
) : JSONDeserializable {
    companion object {
        private const val ID_FIELD = "id"
        private const val TYPE_FIELD = "type"
        private const val PARAMS_FIELD = "params"
        private const val DELAY_MS_FIELD = "delayMs"

        @JvmField
        val deserializer =
            JSONObjectDeserializer { json ->
                ResolvedAction(
                    id = json.getString(ID_FIELD),
                    type = json.getString(TYPE_FIELD),
                    params = json.getString(PARAMS_FIELD),
                    delayMs = json.optNullableLong(DELAY_MS_FIELD),
                )
            }
    }
}

/**
 * Intenal state processor error.
 */
data class StateProcessorError(
    /** An error code. */
    val code: String,
    /** Human-readable description of the failure. */
    val message: String,
    /** Opaque identifier for correlating with backend diagnostics. */
    val diagnosticsId: String,
) : JSONDeserializable {
    companion object {
        private const val CODE_FIELD = "code"
        private const val MESSAGE_FIELD = "message"
        private const val DIAGNOSTICS_ID_FIELD = "diagnosticsId"

        @JvmField
        val deserializer =
            JSONObjectDeserializer { json ->
                StateProcessorError(
                    code = json.getString(CODE_FIELD),
                    message = json.getString(MESSAGE_FIELD),
                    diagnosticsId = json.getString(DIAGNOSTICS_ID_FIELD),
                )
            }
    }
}

internal data class Terminal(val outcome: Outcome)

internal data class ProcessingResult(
    val newState: String,
    val action: ResolvedAction?,
    val terminal: Terminal?,
    val error: StateProcessorError?,
) : JSONDeserializable {

    companion object {
        private const val NEW_STATE_FIELD = "newState"
        private const val ACTION_FIELD = "action"
        private const val ERROR_FIELD = "error"
        private const val TERMINAL_FIELD = "terminal"
        private const val OUTCOME_FIELD = "outcome"

        @JvmField
        val deserializer =
            JSONObjectDeserializer { json ->
                ProcessingResult(
                    newState = json.getJSONObject(NEW_STATE_FIELD).toString(),
                    action = json.optJSONObject(ACTION_FIELD)?.let {
                        ResolvedAction.deserializer.deserialize(it)
                    },
                    terminal = json.optJSONObject(TERMINAL_FIELD)?.let { terminalJson ->
                        val outcomeValue = terminalJson.getString(OUTCOME_FIELD)
                        Terminal(
                            outcome = Outcome.safeValueOf(outcomeValue),
                        )
                    },
                    error = json.optJSONObject(ERROR_FIELD)?.let {
                        StateProcessorError.deserializer.deserialize(it)
                    },
                )
            }
    }
}
