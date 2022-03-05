package io.primer.android.domain.tokenization

import io.primer.android.PaymentMethodIntent
import io.primer.android.completion.ResumeHandlerFactory
import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import io.primer.android.domain.base.BaseInteractor
import io.primer.android.domain.tokenization.models.TokenizationParams
import io.primer.android.domain.tokenization.repository.TokenizationRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.extensions.doOnError
import io.primer.android.extensions.toTokenizationErrorEvent
import io.primer.android.model.dto.PaymentMethodTokenAdapter
import io.primer.android.model.dto.TokenType
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import io.primer.android.threeds.helpers.ThreeDsSdkClassValidator
import io.primer.android.threeds.helpers.ThreeDsSdkClassValidator.Companion.THREE_DS_CLASS_NOT_LOADED_ERROR
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

internal class TokenizationInteractor(
    private val tokenizationRepository: TokenizationRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val threeDsSdkClassValidator: ThreeDsSdkClassValidator,
    private val resumeHandlerFactory: ResumeHandlerFactory,
    private val eventDispatcher: EventDispatcher,
) : BaseInteractor<String, TokenizationParams>() {

    override fun execute(params: TokenizationParams): Flow<String> {
        return tokenizationRepository.tokenize(params)
            .onStart {
                eventDispatcher.dispatchEvent(CheckoutEvent.TokenizationStarted)
            }
            .onEach {
                val token = PaymentMethodTokenAdapter.internalToExternal(it)
                paymentMethodRepository.setPaymentMethod(it)
                val perform3ds = token.paymentInstrumentType == CARD_INSTRUMENT_TYPE &&
                    params.is3DSOnVaultingEnabled &&
                    params.paymentMethodIntent == PaymentMethodIntent.VAULT &&
                    params.paymentMethodDescriptor.config.options?.threeDSecureEnabled == true
                when {
                    perform3ds -> {
                        if (threeDsSdkClassValidator.is3dsSdkIncluded()) {
                            eventDispatcher.dispatchEvents(listOf(CheckoutEvent.Start3DS))
                        } else dispatchEvents(
                            it.setClientThreeDsError(THREE_DS_CLASS_NOT_LOADED_ERROR)
                        )
                    }
                    else -> dispatchEvents(it)
                }
            }
            .doOnError { eventDispatcher.dispatchEvents(listOf(it.toTokenizationErrorEvent())) }
            .map { it.token }
    }

    private fun dispatchEvents(token: PaymentMethodTokenInternal) {
        val externalToken = PaymentMethodTokenAdapter.internalToExternal(token)
        val events = mutableListOf<CheckoutEvent>(
            CheckoutEvent.TokenizationSuccess(
                externalToken,
                resumeHandlerFactory.getResumeHandler(token.paymentInstrumentType)
            )
        )
        if (token.tokenType == TokenType.MULTI_USE) {
            events.add(CheckoutEvent.TokenAddedToVault(externalToken))
        }
        eventDispatcher.dispatchEvents(events)
    }

    private companion object {

        const val CARD_INSTRUMENT_TYPE = "PAYMENT_CARD"
    }
}
