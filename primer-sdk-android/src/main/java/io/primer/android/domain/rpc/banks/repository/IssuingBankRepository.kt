package io.primer.android.domain.rpc.banks.repository

import io.primer.android.domain.rpc.banks.models.IssuingBank
import io.primer.android.domain.rpc.banks.models.IssuingBankParams
import kotlinx.coroutines.flow.Flow

internal interface IssuingBankRepository {

    fun getIssuingBanks(params: IssuingBankParams): Flow<List<IssuingBank>>

    fun getCachedIssuingBanks(): Flow<List<IssuingBank>>
}
