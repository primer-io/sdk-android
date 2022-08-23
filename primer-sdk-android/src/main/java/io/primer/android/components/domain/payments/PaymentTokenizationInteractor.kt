package io.primer.android.components.domain.payments

import io.primer.android.components.domain.core.mapper.PrimerHeadlessUniversalCheckoutPaymentMethodMapper
import io.primer.android.components.domain.core.models.card.PrimerRawCardData
import io.primer.android.components.domain.core.models.otp.PrimerOtpCodeRawData
import io.primer.android.components.domain.core.models.phoneNumber.PrimerRawPhoneNumberData
import io.primer.android.components.domain.exception.InvalidTokenizationDataException
import io.primer.android.components.domain.payments.models.PaymentTokenizationDescriptorParams
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.domain.deeplink.async.repository.AsyncPaymentMethodDeeplinkRepository
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.payments.methods.repository.PaymentMethodsRepository
import io.primer.android.logging.Logger
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.async.blik.AdyenBlikPaymentMethodDescriptor
import io.primer.android.payment.async.ovo.XenditOvoPaymentMethodDescriptor
import io.primer.android.payment.card.CreditCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapLatest

internal class PaymentTokenizationInteractor(
    private val paymentMethodMapper: PrimerHeadlessUniversalCheckoutPaymentMethodMapper,
    private val paymentMethodsRepository: PaymentMethodsRepository,
    private val asyncPaymentMethodDeeplinkRepository: AsyncPaymentMethodDeeplinkRepository,
    private val errorEventResolver: BaseErrorEventResolver,
    private val logger: Logger
) : BaseFlowInteractor<PaymentMethodDescriptor, PaymentTokenizationDescriptorParams>() {

    override fun execute(params: PaymentTokenizationDescriptorParams):
        Flow<PaymentMethodDescriptor> {
        return paymentMethodsRepository.getPaymentMethodDescriptors()
            .mapLatest {
                val descriptor = it.first { it.config.type == params.paymentMethodType }
                val requiredInputDataClass = paymentMethodMapper
                    .getPrimerHeadlessUniversalCheckoutPaymentMethod(params.paymentMethodType)
                    .requiredInputDataClass?.java
                when {
                    descriptor is CreditCard && params.inputData is PrimerRawCardData -> {
                        params.inputData.setTokenizableValues(descriptor)
                    }
                    descriptor is XenditOvoPaymentMethodDescriptor &&
                        params.inputData is PrimerRawPhoneNumberData -> {
                        params.inputData.setTokenizableValues(
                            descriptor,
                            asyncPaymentMethodDeeplinkRepository.getDeeplinkUrl()
                        )
                    }
                    descriptor is AdyenBlikPaymentMethodDescriptor &&
                        params.inputData is PrimerOtpCodeRawData -> {
                        params.inputData.setTokenizableValues(
                            descriptor,
                            asyncPaymentMethodDeeplinkRepository.getDeeplinkUrl()
                        )
                    }
                    else -> throw InvalidTokenizationDataException(
                        params.paymentMethodType,
                        params.inputData::class,
                        requiredInputDataClass?.kotlin
                    )
                }
            }.catch {
                logger.error(CONFIGURATION_ERROR, it)
                errorEventResolver.resolve(it, ErrorMapperType.HUC)
            }
    }

    private companion object {
        const val CONFIGURATION_ERROR =
            "Failed to initialise due to a configuration missing. Please ensure" +
                " that you have called PrimerHeadlessUniversalCheckout start method" +
                " and you have received onAvailablePaymentMethodsLoaded callback before" +
                " calling this method. Please ensure the" +
                " requested payment method has been configured in Primer's dashboard."
    }
}
