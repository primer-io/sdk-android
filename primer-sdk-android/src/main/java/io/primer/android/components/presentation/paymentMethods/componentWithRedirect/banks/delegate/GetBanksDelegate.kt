@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.components.presentation.paymentMethods.componentWithRedirect.banks.delegate

import io.primer.android.domain.payments.methods.PaymentMethodModulesInteractor
import io.primer.android.domain.rpc.banks.BanksFilterInteractor
import io.primer.android.domain.rpc.banks.BanksInteractor
import io.primer.android.domain.rpc.banks.models.IssuingBank
import io.primer.android.domain.rpc.banks.models.IssuingBankFilterParams
import io.primer.android.domain.rpc.banks.models.IssuingBankParams
import io.primer.android.extensions.flatMap
import io.primer.android.payment.PaymentMethodDescriptor
import kotlinx.coroutines.ExperimentalCoroutinesApi

internal class GetBanksDelegate(
    private val paymentMethodType: String,
    private val banksInteractor: BanksInteractor,
    private val banksFilterInteractor: BanksFilterInteractor,
    private val paymentMethodModulesInteractor: PaymentMethodModulesInteractor
) {
    private var paymentMethodDescriptor: PaymentMethodDescriptor? = null

    suspend fun getBanks(query: String? = null): Result<List<IssuingBank>> =
        if (query.isNullOrBlank()) {
            runCatching {
                getPaymentMethodDescriptor()
            }.flatMap { descriptor ->
                banksInteractor(
                    IssuingBankParams(
                        paymentMethodConfigId = requireNotNull(descriptor.config.id),
                        paymentMethod = descriptor.config.type,
                        locale = descriptor.localConfig.settings.locale
                    )
                )
            }
        } else {
            banksFilterInteractor(IssuingBankFilterParams(query))
        }

    private fun getPaymentMethodDescriptor(): PaymentMethodDescriptor {
        return paymentMethodDescriptor
            ?: paymentMethodModulesInteractor.getPaymentMethodDescriptors()
                .first { it.config.type == paymentMethodType }.also { paymentMethodDescriptor = it }
    }
}
