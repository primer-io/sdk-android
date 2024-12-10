package io.primer.android.core.logging

class BlacklistedHttpHeaderProviderRegistry {
    private val providers = mutableListOf<io.primer.android.core.logging.BlacklistedHttpHeadersProvider>()

    fun register(provider: io.primer.android.core.logging.BlacklistedHttpHeadersProvider) {
        providers += provider
    }

    fun unregister(provider: io.primer.android.core.logging.BlacklistedHttpHeadersProvider) {
        providers.removeAll { it === provider }
    }

    fun getAll(): List<io.primer.android.core.logging.BlacklistedHttpHeadersProvider> = providers
}
