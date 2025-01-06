package io.primer.android.card.implementation.tokenization.presentation

import io.primer.android.card.implementation.tokenization.domain.CardTokenizationInteractor
import io.primer.android.card.implementation.tokenization.domain.model.CardPaymentInstrumentParams
import io.primer.android.card.implementation.tokenization.presentation.composable.CardTokenizationInputable
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.payments.core.tokenization.presentation.PaymentMethodTokenizationDelegate
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationCollectedDataMapper
import io.primer.cardShared.extension.removeSpaces

internal class CardTokenizationDelegate(
    tokenizationInteractor: CardTokenizationInteractor,
) : PaymentMethodTokenizationDelegate<CardTokenizationInputable, CardPaymentInstrumentParams>(
        tokenizationInteractor,
    ),
    TokenizationCollectedDataMapper<CardTokenizationInputable, CardPaymentInstrumentParams> {
    override suspend fun mapTokenizationData(
        input: CardTokenizationInputable,
    ): Result<TokenizationParams<CardPaymentInstrumentParams>> =
        runSuspendCatching {
            TokenizationParams(
                CardPaymentInstrumentParams(
                    paymentMethodType = input.paymentMethodType,
                    number = input.cardData.cardNumber.removeSpaces(),
                    expirationMonth =
                        input.cardData.expiryDate.split("/").first().padStart(
                            EXPIRATION_MONTH_PAD_START_LENGTH,
                            EXPIRATION_MONTH_PAD_START_CHAR,
                        ),
                    expirationYear = input.cardData.expiryDate.split("/")[1],
                    cvv = input.cardData.cvv,
                    cardholderName = input.cardData.cardHolderName,
                    preferredNetwork = input.cardData.cardNetwork,
                ),
                input.primerSessionIntent,
            )
        }

    private companion object {
        private const val EXPIRATION_MONTH_PAD_START_LENGTH = 2
        private const val EXPIRATION_MONTH_PAD_START_CHAR = '0'
    }
}
