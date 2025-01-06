package io.primer.android.banks.implementation.rpc.domain

import io.primer.android.banks.implementation.rpc.domain.models.IssuingBank
import io.primer.android.banks.implementation.rpc.domain.models.IssuingBankFilterParams
import io.primer.android.banks.implementation.rpc.domain.repository.IssuingBankRepository
import io.primer.android.core.domain.BaseSuspendInteractor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class BanksFilterInteractor(
    private val issuingBankRepository: IssuingBankRepository,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseSuspendInteractor<List<IssuingBank>, IssuingBankFilterParams>() {
    override suspend fun performAction(params: IssuingBankFilterParams): Result<List<IssuingBank>> =
        issuingBankRepository.getCachedIssuingBanks()
            .map { banks ->
                banks.filter { it.name.contains(params.text, true) }
                    .sortedBy { it.name.lowercase() }
            }
}
