package io.primer.jscore.di

import android.content.Context
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.logging.internal.HttpLoggerInterceptor
import io.primer.jscore.domain.core.JsExecutor
import io.primer.jscore.infrastructure.core.JavaScriptSandboxProvider
import io.primer.jscore.infrastructure.core.JsSandboxExecutor
import io.primer.jscore.infrastructure.core.datasource.JsResourceDataSource
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File

class JsCoreContainer(
    private val sdk: () -> SdkContainer,
) : DependencyContainer() {
    override fun registerInitialDependencies() {
        registerSingleton(name = JS_CACHE_DI_KEY) {
            Cache(
                File(sdk().resolve<Context>().cacheDir, CACHE_DIRECTORY),
                MAX_CACHE_SIZE_MB,
            )
        }

        registerSingleton(name = JS_DOWNLOAD_CLIENT_DI_KEY) {
            OkHttpClient.Builder().apply {
                cache(resolve(name = JS_CACHE_DI_KEY))
                addInterceptor(sdk().resolve<HttpLoggerInterceptor>())
            }.build()
        }

        registerSingleton {
            JavaScriptSandboxProvider(context = sdk().resolve())
        }

        registerFactory {
            JsResourceDataSource(okHttpClient = resolve(name = JS_DOWNLOAD_CLIENT_DI_KEY))
        }

        registerFactory<JsExecutor> {
            JsSandboxExecutor(
                sandboxProvider = resolve(),
                logReporter = sdk().resolve(),
                jsResourceDataSource = resolve(),
            )
        }
    }

    override fun clearUnregisteredDependencies() {
        runCatching { resolve<JavaScriptSandboxProvider>().close() }
    }

    private companion object {
        private const val MAX_CACHE_SIZE_MB = 15 * 1024 * 1024L
        private const val CACHE_DIRECTORY = "primer_sdk_js_cache"
        private const val JS_CACHE_DI_KEY = "JS_CACHE"
        private const val JS_DOWNLOAD_CLIENT_DI_KEY = "JS_DOWNLOAD_CLIENT"
    }
}
