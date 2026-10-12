package io.primer.jscore.infrastructure.core.script

internal object JsScripts {

    // A processor older than probes asks for none.
    val PROBES_TO_RUN = """
        (async (items, client) => {
            if (typeof StateProcessor.probesToRun !== 'function') return JSON.stringify([]);
            return JSON.stringify(await StateProcessor.probesToRun(items, client));
        })
    """.trimIndent()

    val CHECK_CLIENT_REQUIREMENTS = """
        (async (items, client, results) => {
            const result = {};
            for (const item of items) {
                result[item.type] = await StateProcessor.checkClientRequirements(item.clientRequirements, client, results);
            }
            return JSON.stringify(result);
        })
    """.trimIndent()

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
