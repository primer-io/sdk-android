package com.example.myapplication.repositories

import com.example.myapplication.datamodels.PrimerEnv
import com.example.myapplication.datasources.ApiKeyDataSource
import io.paperdb.Paper

/**
 * This is just memory storage for API KEY, please @see(https://primer.io/docs/api)
 */
class AppApiKeyRepository: ApiKeyDataSource {

    private val storage = Paper.book()

    override fun getApiKey(env: PrimerEnv): String? = storage.read(env.name)

    override fun setApiKey(env: PrimerEnv, apiKey: String?) {
        if (apiKey == null) storage.delete(env.name)
        else storage.write(env.name, apiKey)
    }
}
