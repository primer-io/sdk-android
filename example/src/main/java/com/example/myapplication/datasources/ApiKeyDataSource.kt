package com.example.myapplication.datasources

import com.example.myapplication.datamodels.PrimerEnv

interface ApiKeyDataSource {

    fun getApiKey(env: PrimerEnv): String?

    fun setApiKey(env: PrimerEnv, apiKey: String?)
}
