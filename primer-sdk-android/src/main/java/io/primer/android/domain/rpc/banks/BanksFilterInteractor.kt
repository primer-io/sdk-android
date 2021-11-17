package io.primer.android.domain.rpc.banks

import io.primer.android.domain.base.BaseInteractor
import io.primer.android.domain.rpc.banks.models.IssuingBank
import io.primer.android.domain.rpc.banks.models.IssuingBankFilterParams
import io.primer.android.domain.rpc.banks.repository.IssuingBankRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest

internal class BanksFilterInteractor(
    private val issuingBankRepository: IssuingBankRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseInteractor<List<IssuingBank>, IssuingBankFilterParams>() {

    override fun execute(params: IssuingBankFilterParams) =
        issuingBankRepository.getCachedIssuingBanks()
            .mapLatest { it.filter { it.name.contains(params.text, true) } }
            .mapLatest { it.sortedBy { it.name.lowercase() } }
            .flowOn(dispatcher)
}
