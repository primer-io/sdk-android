package io.primer.android.domain.rpc.banks

import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.domain.rpc.banks.models.IssuingBank
import io.primer.android.domain.rpc.banks.models.IssuingBankParams
import io.primer.android.domain.rpc.banks.repository.IssuingBankRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest

internal class BanksInteractor(
    private val issuingBankRepository: IssuingBankRepository,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseFlowInteractor<List<IssuingBank>, IssuingBankParams>() {

    override fun execute(params: IssuingBankParams) =
        issuingBankRepository.getIssuingBanks(params)
            .mapLatest { it.filterNot { it.disabled } }
            .mapLatest { it.sortedBy { it.name.lowercase() } }
            .flowOn(dispatcher)
}
