package io.primer.sample.datasources

import io.primer.sample.datamodels.PrimerEnv

interface ApiKeyDataSource {

    fun getApiKey(env: PrimerEnv): String?

    fun setApiKey(env: PrimerEnv, apiKey: String?)
}
