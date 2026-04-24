package io.primer.jscore.infrastructure.core

import androidx.javascriptengine.JavaScriptIsolate
import androidx.javascriptengine.JavaScriptSandbox
import androidx.javascriptengine.JavaScriptSandbox.JS_FEATURE_PROVIDE_CONSUME_ARRAY_BUFFER
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.core.logging.internal.LogReporter
import io.primer.jscore.BuildConfig
import io.primer.jscore.domain.core.JsExecutor
import io.primer.jscore.domain.core.models.JsResource
import io.primer.jscore.domain.core.models.JsResourceUrl
import io.primer.jscore.infrastructure.core.datasource.JsResourceDataSource
import io.primer.jscore.infrastructure.core.script.JsPolyfills
import io.primer.jscore.infrastructure.core.script.JsScripts
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class JsSandboxExecutor(
    private val sandboxProvider: JavaScriptSandboxProvider,
    private val logReporter: LogReporter,
    private val jsResourceDataSource: JsResourceDataSource,
) : JsExecutor {

    private lateinit var isolate: JavaScriptIsolate

    override suspend fun initialize(resources: List<JsResourceUrl>) = runSuspendCatching {
        val sandbox = sandboxProvider.get()

        val resolvedResources = coroutineScope {
            launch {
                if (::isolate.isInitialized) isolate.close()
                isolate = sandbox.createIsolate()
                if (BuildConfig.DEBUG) {
                    if (sandbox.isFeatureSupported(JavaScriptSandbox.JS_FEATURE_CONSOLE_MESSAGING)) {
                        isolate.setConsoleCallback {
                            logReporter.debug(it.message)
                        }
                    }
                }

                isolate.evaluateJsAsync(JsPolyfills.TEXT_ENCODER_DECODER)
            }
            resources.map { resource ->
                async {
                    jsResourceDataSource.execute(input = resource)
                }
            }
        }.awaitAll()

        val currentIsolate = isolate

        // feed the JS scripts first
        resolvedResources.filterIsInstance<JsResource.Js>().forEach { jsResource ->
            currentIsolate.evaluateJsAsync(jsResource.content)
        }

        // then inject WASM
        resolvedResources.filterIsInstance<JsResource.Wasm>().forEach { wasmResource ->
            if (sandbox.isFeatureSupported(JS_FEATURE_PROVIDE_CONSUME_ARRAY_BUFFER)) {
                currentIsolate.provideNamedData(wasmResource.name, wasmResource.bytes)
                val initScript = """
                (async () => {
                    const buffer = await android.consumeNamedDataAsArrayBuffer('${wasmResource.name}');
                    await wasm_bindgen(buffer);
                })();
                """.trimIndent()
                currentIsolate.evaluateJsAsync(initScript)
            }
        }
    }

    override suspend fun initializeStateProcessor(
        schema: String,
        state: String,
        sdkContext: String,
    ): Result<String> = runSuspendCatching {
        val script = "${JsScripts.INITIALIZE_PROCESSOR}($schema, $state, $sdkContext)"
        isolate.evaluateJsAsync(script)
    }

    override suspend fun applyResult(
        schema: String,
        state: String,
        sdkContext: String,
        actionId: String,
        outcome: String,
        response: String,
    ): Result<String> = runSuspendCatching {
        val safeActionId = JSONObject.quote(actionId)
        val safeOutcome = JSONObject.quote(outcome)
        val script =
            "${JsScripts.APPLY_RESULT}($schema, $state, $sdkContext, $safeActionId, $safeOutcome, $response)"
        isolate.evaluateJsAsync(script)
    }

    override fun destroy() {
        if (::isolate.isInitialized) isolate.close()
    }

    private suspend fun JavaScriptIsolate.evaluateJsAsync(script: String): String =
        evaluateJavaScriptAsync(script).await()

    private suspend fun <T> ListenableFuture<T>.await(): T =
        suspendCancellableCoroutine { continuation ->
            addListener({
                try {
                    continuation.resume(get())
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            }, MoreExecutors.directExecutor())

            continuation.invokeOnCancellation {
                cancel(true)
            }
        }
}
