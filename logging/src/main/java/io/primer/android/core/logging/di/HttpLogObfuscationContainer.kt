package io.primer.android.core.logging.di

import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.logging.BlacklistedHttpHeaderProviderRegistry
import io.primer.android.core.logging.BlacklistedHttpHeadersProvider
import io.primer.android.core.logging.WhitelistedHttpBodyKeyProviderRegistry
import io.primer.android.core.logging.internal.DefaultBlacklistedHttpHeadersProvider

class HttpLogObfuscationContainer : DependencyContainer() {
    override fun registerInitialDependencies() {
        registerSingleton<BlacklistedHttpHeadersProvider>(DEFAULT_NAME) {
            DefaultBlacklistedHttpHeadersProvider()
        }
        registerSingleton { BlacklistedHttpHeaderProviderRegistry() }
        registerSingleton { WhitelistedHttpBodyKeyProviderRegistry() }
    }

    companion object {
        const val DEFAULT_NAME = "DEFAULT"
    }
}
