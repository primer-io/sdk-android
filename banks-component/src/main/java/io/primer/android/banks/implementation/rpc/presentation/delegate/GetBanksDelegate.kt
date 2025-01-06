package io.primer.android.banks.implementation.rpc.presentation.delegate

import io.primer.android.banks.implementation.configuration.domain.BankIssuerConfigurationInteractor
import io.primer.android.banks.implementation.configuration.domain.model.BankIssuerConfig
import io.primer.android.banks.implementation.configuration.domain.model.BankIssuerConfigParams
import io.primer.android.banks.implementation.rpc.domain.BanksFilterInteractor
import io.primer.android.banks.implementation.rpc.domain.BanksInteractor
import io.primer.android.banks.implementation.rpc.domain.models.IssuingBank
import io.primer.android.banks.implementation.rpc.domain.models.IssuingBankFilterParams
import io.primer.android.banks.implementation.rpc.domain.models.IssuingBankParams
import io.primer.android.core.extensions.flatMap

internal class GetBanksDelegate(
    private val paymentMethodType: String,
    private val banksInteractor: BanksInteractor,
    private val banksFilterInteractor: BanksFilterInteractor,
    private val configurationInteractor: BankIssuerConfigurationInteractor,
) {
    private var config: BankIssuerConfig? = null

    suspend fun getBanks(query: String? = null): Result<List<IssuingBank>> =
        if (query.isNullOrBlank()) {
            runCatching {
                getPaymentMethodDescriptor(paymentMethodType)
            }.flatMap { config ->
                banksInteractor(
                    IssuingBankParams(
                        paymentMethodConfigId = config.paymentMethodConfigId,
                        paymentMethod = paymentMethodType,
                        locale = config.locale,
                    ),
                )
            }
        } else {
            banksFilterInteractor(IssuingBankFilterParams(query))
        }

    private suspend fun getPaymentMethodDescriptor(paymentMethodType: String): BankIssuerConfig {
        return config
            ?: configurationInteractor(BankIssuerConfigParams(paymentMethodType = paymentMethodType))
                .getOrThrow()
    }
}
