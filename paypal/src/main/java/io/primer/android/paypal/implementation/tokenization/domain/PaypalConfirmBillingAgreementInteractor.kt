package io.primer.android.paypal.implementation.tokenization.domain

import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalConfirmBillingAgreement
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalConfirmBillingAgreementParams
import io.primer.android.paypal.implementation.tokenization.domain.repository.PaypalConfirmBillingAgreementRepository
import io.primer.android.paypal.implementation.validation.resolvers.PaypalCheckoutOrderInfoValidationRulesResolver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class PaypalConfirmBillingAgreementInteractor(
    private val confirmBillingAgreementRepository: PaypalConfirmBillingAgreementRepository,
    private val validationRulesResolver: PaypalCheckoutOrderInfoValidationRulesResolver,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseSuspendInteractor<PaypalConfirmBillingAgreement, PaypalConfirmBillingAgreementParams>() {
    override suspend fun performAction(
        params: PaypalConfirmBillingAgreementParams,
    ): Result<PaypalConfirmBillingAgreement> =
        runSuspendCatching {
            validationRulesResolver.resolve().rules.map { rule ->
                rule.validate(params.tokenId)
            }.forEach { validationResult ->
                if (validationResult is ValidationResult.Failure) throw validationResult.exception
            }

            return confirmBillingAgreementRepository.confirmBillingAgreement(params)
        }
}
