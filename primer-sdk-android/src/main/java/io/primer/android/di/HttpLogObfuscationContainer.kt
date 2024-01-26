package io.primer.android.di

import io.primer.android.core.logging.BlacklistedHttpHeaderProviderRegistry
import io.primer.android.core.logging.BlacklistedHttpHeadersProvider
import io.primer.android.core.logging.WhitelistedHttpBodyKeyProviderRegistry
import io.primer.android.core.logging.internal.DefaultBlacklistedHttpHeadersProvider

internal class HttpLogObfuscationContainer : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton<BlacklistedHttpHeadersProvider>(DEFAULT_NAME) {
            DefaultBlacklistedHttpHeadersProvider()
        }
        registerFactory { BlacklistedHttpHeaderProviderRegistry() }
        registerFactory { WhitelistedHttpBodyKeyProviderRegistry() }
    }

    companion object {
        const val DEFAULT_NAME = "DEFAULT"
    }
}
