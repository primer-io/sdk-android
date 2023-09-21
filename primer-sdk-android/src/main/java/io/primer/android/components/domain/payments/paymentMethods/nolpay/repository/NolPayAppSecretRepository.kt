package io.primer.android.components.domain.payments.paymentMethods.nolpay.repository

internal interface NolPayAppSecretRepository {

    suspend fun getAppSecret(sdkId: String, appId: String): Result<String>
}
