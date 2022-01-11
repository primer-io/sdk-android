package io.primer.android.domain.payments.methods

import io.primer.android.completion.ResumeHandlerFactory
import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import io.primer.android.domain.base.BaseInteractor
import io.primer.android.domain.payments.methods.models.VaultTokenParams
import io.primer.android.domain.payments.methods.repository.VaultedPaymentMethodsRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.extensions.toTokenizationErrorEvent
import io.primer.android.model.dto.PaymentMethodTokenAdapter
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach

@ExperimentalCoroutinesApi
internal class VaultedPaymentMethodsExchangeInteractor(
    private val vaultedPaymentMethodsRepository: VaultedPaymentMethodsRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val resumeHandlerFactory: ResumeHandlerFactory,
    private val eventDispatcher: EventDispatcher,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseInteractor<PaymentMethodTokenInternal, VaultTokenParams>() {

    override fun execute(params: VaultTokenParams): Flow<PaymentMethodTokenInternal> {
        return vaultedPaymentMethodsRepository.exchangeVaultedPaymentToken(
            params.token.token
        ).flowOn(dispatcher)
            .onEach {
                paymentMethodRepository.setPaymentMethod(it)
                val externalToken = PaymentMethodTokenAdapter.internalToExternal(it)
                eventDispatcher.dispatchEvent(
                    CheckoutEvent.TokenSelected(
                        externalToken,
                        resumeHandlerFactory.getResumeHandler(it.paymentInstrumentType)
                    )
                )
            }
            .catch {
                eventDispatcher.dispatchEvent(it.toTokenizationErrorEvent())
            }
    }
}
