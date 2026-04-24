package io.primer.jscore.domain.core

import io.primer.jscore.domain.core.models.JsResourceUrl

interface JsExecutor {

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

    fun destroy()
}
