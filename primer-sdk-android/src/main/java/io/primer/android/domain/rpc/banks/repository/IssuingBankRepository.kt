package io.primer.android.domain.rpc.banks.repository

import io.primer.android.domain.rpc.banks.models.IssuingBank
import io.primer.android.domain.rpc.banks.models.IssuingBankParams

internal interface IssuingBankRepository {

    suspend fun getIssuingBanks(params: IssuingBankParams): Result<List<IssuingBank>>

    suspend fun getCachedIssuingBanks(): Result<List<IssuingBank>>
}
