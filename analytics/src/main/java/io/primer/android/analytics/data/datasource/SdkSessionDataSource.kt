package io.primer.android.analytics.data.datasource

import java.util.UUID

internal class SdkSessionDataSource private constructor() {
    companion object {
        private val sdkSessionId by lazy { UUID.randomUUID().toString() }

        fun getSessionId() = sdkSessionId
    }
}
