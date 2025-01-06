package io.primer.android.nolpay.implementation.common.domain.repository

internal fun interface NolPayAppSecretRepository {
    suspend fun getAppSecret(
        sdkId: String,
        appId: String,
    ): Result<String>
}
