package io.primer.android.domain.payments.methods

import io.primer.android.domain.base.BaseInteractor
import io.primer.android.domain.base.None
import io.primer.android.domain.payments.methods.repository.VaultedPaymentMethodsRepository
import io.primer.android.events.EventDispatcher
import io.primer.android.extensions.toCheckoutErrorEvent
import io.primer.android.model.dto.PaymentMethodTokenInternal
import io.primer.android.model.dto.PaymentMethodType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest

@ExperimentalCoroutinesApi
internal class VaultedPaymentMethodsInteractor(
    private val vaultedPaymentMethodsRepository: VaultedPaymentMethodsRepository,
    private val eventDispatcher: EventDispatcher,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseInteractor<List<PaymentMethodTokenInternal>, None>() {

    override fun execute(params: None) = vaultedPaymentMethodsRepository.getVaultedPaymentMethods()
        .mapLatest {
            it.filter {
                DISALLOWED_PAYMENT_METHOD_TYPES.contains(it.paymentInstrumentType).not()
            }
        }.flowOn(dispatcher)
        .catch {
            eventDispatcher.dispatchEvent(it.toCheckoutErrorEvent(CONFIGURATION_ERROR))
        }

    companion object {

        const val CONFIGURATION_ERROR =
            "Failed to initialise due to a failed network call. Please ensure" +
                "your internet connection is stable and try again."
        private val DISALLOWED_PAYMENT_METHOD_TYPES = listOf(PaymentMethodType.APAYA.name)
    }
}
