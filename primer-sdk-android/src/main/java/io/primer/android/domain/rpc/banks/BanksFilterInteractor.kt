package io.primer.android.domain.rpc.banks

import io.primer.android.domain.base.BaseSuspendInteractor
import io.primer.android.domain.rpc.banks.models.IssuingBank
import io.primer.android.domain.rpc.banks.models.IssuingBankFilterParams
import io.primer.android.domain.rpc.banks.repository.IssuingBankRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class BanksFilterInteractor(
    private val issuingBankRepository: IssuingBankRepository,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<List<IssuingBank>, IssuingBankFilterParams>() {
    override suspend fun performAction(params: IssuingBankFilterParams): Result<List<IssuingBank>> =
        issuingBankRepository.getCachedIssuingBanks()
            .map { banks ->
                banks.filter { it.name.contains(params.text, true) }
                    .sortedBy { it.name.lowercase() }
            }
}
