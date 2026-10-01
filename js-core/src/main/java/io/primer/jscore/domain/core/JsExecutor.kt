package io.primer.jscore.domain.core

import io.primer.jscore.domain.core.models.JsResourceUrl

interface JsExecutor {

    /** False until [initialize] succeeds, and again once the engine behind it dies; [initialize] recovers. */
    val isAlive: Boolean

    suspend fun initialize(resources: List<JsResourceUrl>): Result<Unit>

    suspend fun initializeStateProcessor(
        schema: String,
        state: String,
        sdkContext: String,
    ): Result<String>

    suspend fun applyResult(
        schema: String,
        state: String,
        sdkContext: String,
        actionId: String,
        outcome: String,
        response: String,
    ): Result<String>

    /** Each item's verdict by type; [items] are `{ id, type, clientRequirements }`, both arguments JSON. */
    suspend fun checkClientRequirements(items: String, client: String): Result<String>

    fun destroy()
}
