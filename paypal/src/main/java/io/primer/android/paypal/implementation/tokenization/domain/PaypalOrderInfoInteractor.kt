package io.primer.android.paypal.implementation.tokenization.domain

import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalOrderInfo
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalOrderInfoParams
import io.primer.android.paypal.implementation.tokenization.domain.repository.PaypalInfoRepository
import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.paypal.implementation.validation.resolvers.PaypalCheckoutOrderInfoValidationRulesResolver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class PaypalOrderInfoInteractor(
    private val paypalInfoRepository: PaypalInfoRepository,
    private val validationRulesResolver: PaypalCheckoutOrderInfoValidationRulesResolver,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<PaypalOrderInfo, PaypalOrderInfoParams>() {

    override suspend fun performAction(params: PaypalOrderInfoParams): Result<PaypalOrderInfo> = runSuspendCatching {
        validationRulesResolver.resolve().rules.map { rule ->
            rule.validate(params.orderId)
        }.forEach { validationResult ->
            if (validationResult is ValidationResult.Failure) throw validationResult.exception
        }

        return paypalInfoRepository.getPaypalOrderInfo(params)
    }
}
