package io.primer.android.domain.token

import kotlinx.coroutines.flow.Flow

internal interface ValidateTokenRepository {

    fun validate(clientToken: String): Flow<Boolean>
}
