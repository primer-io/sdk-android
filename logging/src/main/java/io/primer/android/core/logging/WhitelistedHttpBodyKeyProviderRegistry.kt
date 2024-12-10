package io.primer.android.core.logging

class WhitelistedHttpBodyKeyProviderRegistry {
    private val providers = mutableListOf<WhitelistedHttpBodyKeysProvider>()

    fun register(provider: WhitelistedHttpBodyKeysProvider) {
        providers += provider
    }

    fun unregister(provider: WhitelistedHttpBodyKeysProvider) {
        providers.removeAll { it === provider }
    }

    fun getAll(): List<WhitelistedHttpBodyKeysProvider> = providers
}
