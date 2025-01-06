package io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.parser

import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.model.PaymentMethodResumeClientToken

fun interface PaymentMethodClientTokenParser<T : PaymentMethodResumeClientToken> {
    fun parseClientToken(clientToken: String): T
}
