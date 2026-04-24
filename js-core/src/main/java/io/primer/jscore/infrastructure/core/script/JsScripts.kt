package io.primer.jscore.infrastructure.core.script

internal object JsScripts {

    val INITIALIZE_PROCESSOR = """
        (async (schema, state, sdkContext) => {
            const processor = await StateProcessor.createStateProcessor(JSON.stringify(schema), sdkContext);
            const result = await processor.initialize(state);
            return JSON.stringify(result);
        })
    """.trimIndent()

    val APPLY_RESULT = """
        (async (schema, state, sdkContext, actionId, outcome, response) => {
            const processor = await StateProcessor.createStateProcessor(JSON.stringify(schema), sdkContext);
            const result = await processor.applyResult(
                state,
                actionId,
                outcome,
                response
            );
            return JSON.stringify(result);
        })
    """.trimIndent()

    val APPLY_EVENT = """
        (async (schema, state, event) => {
            const processor = await StateProcessor.createStateProcessor(JSON.stringify(schema));
            const result = await processor.applyEvent(
                state,
                event
            );
            return JSON.stringify(result);
        })
    """.trimIndent()
}
