package io.primer.android.core.logging.internal

import io.primer.android.core.logging.BlacklistedHttpHeaderProviderRegistry
import io.primer.android.core.logging.BuildConfig
import io.primer.android.core.logging.ConsolePrimerLogger
import io.primer.android.core.logging.PrimerLogging
import io.primer.android.core.logging.WhitelistedHttpBodyKeyProviderRegistry
import io.primer.android.core.logging.extensions.logBody
import io.primer.android.core.logging.extensions.logHeaders
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

@Suppress("LongParameterList")
class HttpLoggerInterceptor(
    private val logReporter: LogReporter,
    private val level: Level = Level.BODY,
    private val blacklistedHttpHeaderProviderRegistry: BlacklistedHttpHeaderProviderRegistry,
    private val whitelistedHttpBodyKeyProviderRegistry: WhitelistedHttpBodyKeyProviderRegistry,
    private val pciUrlProvider: () -> String?,
    private val getCurrentTimeMillis: () -> Long = { System.currentTimeMillis() },
    private val isDebugBuild: Boolean = BuildConfig.DEBUG,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val blacklistedHeaders =
            blacklistedHttpHeaderProviderRegistry.getAll()
                .flatMap { it.values }
        val whitelistedBodyKeys =
            whitelistedHttpBodyKeyProviderRegistry.getAll()
                .flatMap { it.values }

        val request = chain.request()
        val bodyObfuscationLevel = request.bodyObfuscationLevel
        val requestStartTime = getCurrentTimeMillis()

        val shouldLogBody = level == Level.BODY
        val shouldLogHeaders = shouldLogBody || level >= Level.HEADERS

        with(request) {
            logHeaders(
                logReporter = logReporter,
                shouldLogHeaders = shouldLogHeaders,
                blacklistedHeaders = blacklistedHeaders,
                obfuscationLevel = headersObfuscationLevel,
                obfuscationString = OBFUSCATION_STRING,
            )
            logBody(
                logReporter = logReporter,
                shouldLogBody = shouldLogBody,
                whitelistedBodyKeys = whitelistedBodyKeys,
                obfuscationLevel = bodyObfuscationLevel,
                obfuscationString = OBFUSCATION_STRING,
            )
        }

        val response: Response?
        try {
            response = chain.proceed(request)

            with(response) {
                logHeaders(
                    logReporter = logReporter,
                    requestTime = getCurrentTimeMillis() - requestStartTime,
                    shouldLogHeaders = shouldLogHeaders,
                    blacklistedHeaders = blacklistedHeaders,
                    obfuscationLevel = headersObfuscationLevel,
                    obfuscationString = OBFUSCATION_STRING,
                )
                logBody(
                    logReporter = logReporter,
                    shouldLogBody = shouldLogBody,
                    whitelistedBodyKeys = whitelistedBodyKeys,
                    obfuscationLevel = bodyObfuscationLevel,
                    obfuscationString = OBFUSCATION_STRING,
                )
            }
        } catch (e: IOException) {
            logReporter.debug(
                """
                <-- ${request.method} ${request.url}
                HTTP failed: ${e.message}
                <-- END HTTP
                """.trimIndent(),
            )
            throw e
        }

        return response
    }

    private val Request.bodyObfuscationLevel: ObfuscationLevel
        get() =
            when {
                isDebugBuild || PrimerLogging.logger is ConsolePrimerLogger -> {
                    ObfuscationLevel.NONE
                }

                containsPciUrl -> ObfuscationLevel.REDACT_BODY

                else -> ObfuscationLevel.LIST
            }

    private val headersObfuscationLevel: ObfuscationLevel
        get() =
            when {
                isDebugBuild || PrimerLogging.logger is ConsolePrimerLogger -> {
                    ObfuscationLevel.NONE
                }

                else -> ObfuscationLevel.LIST
            }

    private val Request.containsPciUrl: Boolean
        get() {
            val pciUrl =
                pciUrlProvider() ?: run {
                    logReporter.warn("Failed to get configuration")
                    return false
                }

            return url.toUrl().toString().startsWith(pciUrl)
        }

    private companion object {
        const val OBFUSCATION_STRING = "****"
    }

    internal enum class ObfuscationLevel {
        /**
         * Obfuscation is disabled
         */
        NONE,

        /**
         * Headers (bodies) are obfuscated based on a blacklist (whitelist)
         */
        LIST,

        /**
         * The entire body is redacted
         */
        REDACT_BODY,
    }

    enum class Level {
        /**
         * Logs request and response lines.
         *
         * Example:
         * ```
         * --> POST /greeting (3-byte body)
         *
         * <-- 200 POST /greeting (22ms, 6-byte body)
         * ```
         */
        BASIC,

        /**
         * Logs request and response lines and their respective headers.
         *
         * Example:
         * ```
         * --> POST /greeting (3-byte body)
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         * --> END POST
         *
         * <-- 200 POST /greeting (22ms, 6-byte body)
         * Content-Type: plain/text
         * Content-Length: 6
         * <-- END HTTP
         * ```
         */
        HEADERS,

        /**
         * Logs request and response lines and their respective headers and bodies (if present).
         *
         * Example:
         * ```
         * --> POST /greeting (3-byte body)
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         *
         * Hi?
         * --> END POST
         *
         * <-- 200 POST /greeting (22ms, 6-byte body)
         * Content-Type: plain/text
         * Content-Length: 6
         *
         * Hello!
         * <-- END HTTP
         * ```
         */
        BODY,
    }
}
