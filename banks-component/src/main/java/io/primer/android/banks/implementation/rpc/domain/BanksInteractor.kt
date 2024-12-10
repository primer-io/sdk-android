package io.primer.android.banks.implementation.rpc.domain

import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.banks.implementation.rpc.domain.models.IssuingBank
import io.primer.android.banks.implementation.rpc.domain.models.IssuingBankParams
import io.primer.android.banks.implementation.rpc.domain.repository.IssuingBankRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class BanksInteractor(
    private val issuingBankRepository: IssuingBankRepository,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<List<IssuingBank>, IssuingBankParams>() {
    override suspend fun performAction(params: IssuingBankParams): Result<List<IssuingBank>> =
        issuingBankRepository.getIssuingBanks(params)
            .map { banks ->
                banks.filterNot { it.disabled }
                    .sortedBy { it.name.lowercase() }
            }
}
