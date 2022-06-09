package io.primer.android.domain.token.repository

import kotlinx.coroutines.flow.Flow

internal interface ValidateTokenRepository {

    fun validate(clientToken: String): Flow<Boolean>
}
