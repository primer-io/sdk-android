package io.primer.android.domain.tokenization

import io.primer.android.domain.tokenization.models.TokenizationParams
import io.primer.android.domain.tokenization.models.toTokenizationRequest
import io.primer.android.domain.tokenization.repository.TokenizationRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.extensions.doOnError
import io.primer.android.extensions.toCheckoutErrorEvent
import io.primer.android.model.dto.PaymentMethodTokenAdapter
import io.primer.android.model.dto.PaymentMethodTokenInternal
import io.primer.android.model.dto.TokenType
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import io.primer.android.ui.fragments.ErrorType
import io.primer.android.ui.fragments.SuccessType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

internal class TokenizationInteractor(
    private val tokenizationRepository: TokenizationRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val eventDispatcher: EventDispatcher,
) {

    fun tokenize(params: TokenizationParams): Flow<String> {
        return tokenizationRepository.tokenize(params.toTokenizationRequest())
            .onEach {
                val token = PaymentMethodTokenAdapter.internalToExternal(it)
                val perform3ds = token.paymentInstrumentType == CARD_INSTRUMENT_TYPE &&
                    params.is3DSAtTokenizationEnabled &&
                    params.paymentMethodDescriptor.config.options?.threeDSecureEnabled == true
                when {
                    perform3ds -> {
                        paymentMethodRepository.setPaymentMethod(it)
                        eventDispatcher.dispatchEvents(listOf(CheckoutEvent.Start3DS))
                    }
                    else -> dispatchEvents(it)
                }
            }
            .doOnError { eventDispatcher.dispatchEvents(listOf(it.toCheckoutErrorEvent())) }
            .map { it.token }
    }

    private fun dispatchEvents(token: PaymentMethodTokenInternal) {
        val externalToken = PaymentMethodTokenAdapter.internalToExternal(token)
        val events = mutableListOf<CheckoutEvent>(
            CheckoutEvent.TokenizationSuccess(
                externalToken,
                ::completionHandler
            )
        )
        if (token.tokenType == TokenType.MULTI_USE) {
            events.add(CheckoutEvent.TokenAddedToVault(externalToken))
        }
        eventDispatcher.dispatchEvents(events)
    }

    private fun completionHandler(error: Error?) {
        if (error == null) {
            eventDispatcher.dispatchEvents(
                listOf(CheckoutEvent.ShowSuccess(successType = SuccessType.PAYMENT_SUCCESS))
            )
        } else {
            eventDispatcher.dispatchEvents(
                listOf(CheckoutEvent.ShowError(errorType = ErrorType.PAYMENT_FAILED))
            )
        }
    }

    private companion object {

        const val CARD_INSTRUMENT_TYPE = "PAYMENT_CARD"
    }
}
