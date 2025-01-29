package io.primer.sample.repositories

import io.paperdb.Paper
import io.primer.sample.datamodels.PrimerEnv
import io.primer.sample.datasources.ApiKeyDataSource

/**
 * This is just memory storage for API KEY, please @see(https://primer.io/docs/api)
 */
class AppApiKeyRepository : ApiKeyDataSource {

    private val storage = Paper.book()

    override fun getApiKey(env: PrimerEnv): String? = storage.read(env.name)

    override fun setApiKey(env: PrimerEnv, apiKey: String?) {
        if (apiKey == null) storage.delete(env.name)
        else storage.write(env.name, apiKey)
    }
}
