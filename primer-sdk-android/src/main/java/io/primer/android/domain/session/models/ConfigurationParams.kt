package io.primer.android.domain.session.models

import io.primer.android.domain.base.Params
import io.primer.android.domain.session.CachePolicy

internal data class ConfigurationParams(val cachePolicy: CachePolicy) : Params
