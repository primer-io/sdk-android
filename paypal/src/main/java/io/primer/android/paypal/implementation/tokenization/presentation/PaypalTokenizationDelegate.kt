package io.primer.android.paypal.implementation.tokenization.presentation

import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.payments.core.tokenization.presentation.PaymentMethodTokenizationDelegate
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationCollectedDataMapper
import io.primer.android.paypal.implementation.tokenization.domain.PaypalConfirmBillingAgreementInteractor
import io.primer.android.paypal.implementation.tokenization.domain.PaypalOrderInfoInteractor
import io.primer.android.paypal.implementation.tokenization.domain.PaypalTokenizationInteractor
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalConfirmBillingAgreementParams
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalOrderInfoParams
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalPaymentInstrumentParams
import io.primer.android.paypal.implementation.tokenization.presentation.model.PaypalTokenizationInputable

internal class PaypalTokenizationDelegate(
    private val tokenizationInteractor: PaypalTokenizationInteractor,
    private val paypalCreateOrderInteractor: PaypalOrderInfoInteractor,
    private val confirmBillingAgreementInteractor: PaypalConfirmBillingAgreementInteractor
) : PaymentMethodTokenizationDelegate<PaypalTokenizationInputable, PaypalPaymentInstrumentParams>(
    tokenizationInteractor
),
    TokenizationCollectedDataMapper<PaypalTokenizationInputable,
        PaypalPaymentInstrumentParams> {

    override suspend fun mapTokenizationData(input: PaypalTokenizationInputable):
        Result<TokenizationParams<PaypalPaymentInstrumentParams>> = when (input) {
        is PaypalTokenizationInputable.PaypalCheckoutTokenizationInputable -> getPaypalOrderInfo(
            paymentMethodConfigId = input.paymentMethodConfigId,
            orderId = input.orderId
        ).map { orderInfo ->
            TokenizationParams(
                paymentInstrumentParams = PaypalPaymentInstrumentParams.PaypalCheckoutPaymentInstrumentParams(
                    paymentMethodType = input.paymentMethodType,
                    externalPayerId = orderInfo.externalPayerId,
                    externalPayerInfoEmail = orderInfo.email,
                    paypalOrderId = orderInfo.orderId,
                    externalPayerFirstName = orderInfo.externalPayerFirstName,
                    externalPayerLastName = orderInfo.externalPayerLastName
                ),
                sessionIntent = input.primerSessionIntent
            )
        }

        is PaypalTokenizationInputable.PaypalVaultTokenizationInputable ->
            confirmBillingAgreement(
                paymentMethodConfigId = input.paymentMethodConfigId,
                token = input.tokenId
            ).map { billingAgreement ->
                TokenizationParams(
                    paymentInstrumentParams = PaypalPaymentInstrumentParams.PaypalVaultPaymentInstrumentParams(
                        paypalBillingAgreementId = billingAgreement.billingAgreementId,
                        paymentMethodType = input.paymentMethodType,
                        externalPayerInfo = billingAgreement.externalPayerInfo,
                        shippingAddress = billingAgreement.shippingAddress
                    ),
                    sessionIntent = input.primerSessionIntent
                )
            }
    }

    private suspend fun getPaypalOrderInfo(paymentMethodConfigId: String, orderId: String?) =
        paypalCreateOrderInteractor(
            params = PaypalOrderInfoParams(
                paymentMethodConfigId = paymentMethodConfigId,
                orderId = orderId
            )
        )

    private suspend fun confirmBillingAgreement(
        paymentMethodConfigId: String,
        token: String?
    ) = confirmBillingAgreementInteractor(
        params = PaypalConfirmBillingAgreementParams(
            paymentMethodConfigId = paymentMethodConfigId,
            tokenId = requireNotNull(token)
        )
    )
}
