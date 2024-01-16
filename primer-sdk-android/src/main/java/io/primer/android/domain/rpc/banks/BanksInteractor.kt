package io.primer.android.domain.rpc.banks

import io.primer.android.domain.base.BaseSuspendInteractor
import io.primer.android.domain.rpc.banks.models.IssuingBank
import io.primer.android.domain.rpc.banks.models.IssuingBankParams
import io.primer.android.domain.rpc.banks.repository.IssuingBankRepository
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
