package io.primer.android.banks.implementation.rpc.domain.repository

import io.primer.android.banks.implementation.rpc.domain.models.IssuingBank
import io.primer.android.banks.implementation.rpc.domain.models.IssuingBankParams

internal interface IssuingBankRepository {
    suspend fun getIssuingBanks(params: IssuingBankParams): Result<List<IssuingBank>>

    suspend fun getCachedIssuingBanks(): Result<List<IssuingBank>>
}
