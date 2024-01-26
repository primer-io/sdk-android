package io.primer.android.core.logging

internal class BlacklistedHttpHeaderProviderRegistry {
    private val providers = mutableListOf<BlacklistedHttpHeadersProvider>()

    fun register(provider: BlacklistedHttpHeadersProvider) {
        providers += provider
    }

    fun unregister(provider: BlacklistedHttpHeadersProvider) {
        providers.removeAll { it === provider }
    }

    fun getAll(): List<BlacklistedHttpHeadersProvider> = providers
}
