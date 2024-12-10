package io.primer.android.clientToken.core.validation.domain.repository

fun interface ValidateClientTokenRepository {

    suspend fun validate(clientToken: String): Result<Boolean>
}
