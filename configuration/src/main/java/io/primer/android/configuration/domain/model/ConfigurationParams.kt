package io.primer.android.configuration.domain.model

import io.primer.android.configuration.domain.CachePolicy
import io.primer.android.core.domain.Params

data class ConfigurationParams(val cachePolicy: CachePolicy) : Params
