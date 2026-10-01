package io.primer.jscore.infrastructure.core

import android.annotation.SuppressLint
import android.os.Build
import android.webkit.WebView
import androidx.javascriptengine.IsolateTerminatedException
import androidx.javascriptengine.JavaScriptConsoleCallback
import androidx.javascriptengine.JavaScriptIsolate
import androidx.javascriptengine.JavaScriptSandbox
import androidx.javascriptengine.JavaScriptSandbox.JS_FEATURE_PROVIDE_CONSUME_ARRAY_BUFFER
import androidx.javascriptengine.SandboxDeadException
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.core.logging.internal.LogReporter
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
import java.util.concurrent.ExecutionException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class JsSandboxExecutor(
    private val sandboxProvider: JavaScriptSandboxProvider,
    private val logReporter: LogReporter,
    private val jsResourceDataSource: JsResourceDataSource,
) : JsExecutor {

    private lateinit var sandbox: JavaScriptSandbox
    private lateinit var isolate: JavaScriptIsolate

    @Volatile
    override var isAlive: Boolean = false
        private set

    @SuppressLint("RequiresFeature")
    override suspend fun initialize(resources: List<JsResourceUrl>) = runSuspendCatching {
        isAlive = false
        val sandbox = sandboxProvider.get().also { this.sandbox = it }

        val resolvedResources = coroutineScope {
            launch {
                if (::isolate.isInitialized) isolate.close()
                isolate = sandbox.createIsolate()
                if (sandbox.isFeatureSupported(JavaScriptSandbox.JS_FEATURE_CONSOLE_MESSAGING)) {
                    isolate.setConsoleCallback(::onJsConsoleMessage)
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
        val wasmResources = resolvedResources.filterIsInstance<JsResource.Wasm>()
        if (wasmResources.isNotEmpty() &&
            !sandbox.isFeatureSupported(JS_FEATURE_PROVIDE_CONSUME_ARRAY_BUFFER)
        ) {
            val webViewPackage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WebView.getCurrentWebViewPackage()?.packageName.orEmpty()
            } else {
                ""
            }
            error(
                "JavaScriptSandbox does not support JS_FEATURE_PROVIDE_CONSUME_ARRAY_BUFFER; " +
                    "cannot load ${wasmResources.size} WASM resource(s). " +
                    "The Android System WebView is likely too old (provider=$webViewPackage).",
            )
        }

        // feed the JS scripts first
        resolvedResources.filterIsInstance<JsResource.Js>().forEach { jsResource ->
            currentIsolate.evaluateJsAsync(jsResource.content)
        }

        // then inject WASM
        wasmResources.forEach { wasmResource ->
            currentIsolate.provideNamedData(wasmResource.name, wasmResource.bytes)
            val initScript = """
            (async () => {
                const buffer = await android.consumeNamedDataAsArrayBuffer('${wasmResource.name}');
                await wasm_bindgen(buffer);
            })();
            """.trimIndent()
            currentIsolate.evaluateJsAsync(initScript)
        }
        isAlive = true
    }

    private fun onJsConsoleMessage(message: JavaScriptConsoleCallback.ConsoleMessage) {
        val formatted = "[JS:${message.line}:${message.column}] ${message.message}"
        when (message.level) {
            JavaScriptConsoleCallback.ConsoleMessage.LEVEL_ERROR -> logReporter.error(formatted)
            JavaScriptConsoleCallback.ConsoleMessage.LEVEL_WARNING -> logReporter.warn(formatted)
            JavaScriptConsoleCallback.ConsoleMessage.LEVEL_INFO -> logReporter.info(formatted)
            else -> logReporter.debug(formatted)
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

    override suspend fun checkClientRequirements(items: String, client: String): Result<String> =
        runSuspendCatching {
            isolate.evaluateJsAsync("${JsScripts.CHECK_CLIENT_REQUIREMENTS}($items, $client)")
        }

    override fun destroy() {
        if (::isolate.isInitialized) isolate.close()
    }

    private suspend fun JavaScriptIsolate.evaluateJsAsync(script: String): String =
        try {
            evaluateJavaScriptAsync(script).await()
        } catch (expected: ExecutionException) {
            expected.isolateTermination?.let { onTerminated(it) }
            throw expected
        }

    // The next initialize recovers, on a new sandbox when this one died.
    private suspend fun onTerminated(termination: IsolateTerminatedException) {
        isAlive = false
        logReporter.warn("JS engine terminated: ${termination.message}")
        if (termination is SandboxDeadException) sandboxProvider.drop(sandbox)
    }

    // Searched along the causes, since coroutines may wrap what the future failed with.
    private val Throwable.isolateTermination: IsolateTerminatedException?
        get() = generateSequence(this) { it.cause }.filterIsInstance<IsolateTerminatedException>().firstOrNull()

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
