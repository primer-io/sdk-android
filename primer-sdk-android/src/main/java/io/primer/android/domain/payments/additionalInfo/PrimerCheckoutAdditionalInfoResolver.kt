package io.primer.android.domain.payments.additionalInfo

import io.primer.android.PrimerSessionIntent
import io.primer.android.components.data.payments.paymentMethods.nativeUi.stripe.ach.exception.StripeIllegalValueKey
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.CompleteStripeAchPaymentSessionDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.StripeAchMandateTimestampLoggingDelegate
import io.primer.android.data.base.util.requireNotNullCheck
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.exception.PaymentMethodCancelledException
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.token.model.ClientToken
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.payments.create.repository.PaymentResultRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.extensions.flatMap
import io.primer.android.extensions.runSuspendCatching
import io.primer.android.ui.fragments.SuccessType
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal interface AdditionalInfoResolverExtraParams {
    object None : AdditionalInfoResolverExtraParams
}

internal interface PrimerCheckoutAdditionalInfoResolver {

    fun resolve(
        clientToken: ClientToken,
        extraParams: AdditionalInfoResolverExtraParams = AdditionalInfoResolverExtraParams.None
    ): PrimerCheckoutAdditionalInfo?
}

internal class OmiseCheckoutAdditionalInfoResolver : PrimerCheckoutAdditionalInfoResolver {

    override fun resolve(
        clientToken: ClientToken,
        extraParams: AdditionalInfoResolverExtraParams
    ): PrimerCheckoutAdditionalInfo {
        return PromptPayCheckoutAdditionalInfo(
            clientToken.expiration.orEmpty(),
            clientToken.qrCodeUrl,
            clientToken.qrCode
        )
    }
}

internal class MultibancoCheckoutAdditionalInfoResolver : PrimerCheckoutAdditionalInfoResolver {

    override fun resolve(
        clientToken: ClientToken,
        extraParams: AdditionalInfoResolverExtraParams
    ): PrimerCheckoutAdditionalInfo {
        return MultibancoCheckoutAdditionalInfo(
            clientToken.expiresAt.orEmpty(),
            clientToken.reference.orEmpty(),
            clientToken.entity.orEmpty()
        )
    }
}

internal class RetailOutletsCheckoutAdditionalInfoResolver : PrimerCheckoutAdditionalInfoResolver {

    private val dateFormatISO = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    private val expiresDateFormat = DateFormat.getDateTimeInstance(
        DateFormat.MEDIUM,
        DateFormat.SHORT
    )

    var retailerName: String? = null

    override fun resolve(
        clientToken: ClientToken,
        extraParams: AdditionalInfoResolverExtraParams
    ): PrimerCheckoutAdditionalInfo {
        return XenditCheckoutVoucherAdditionalInfo(
            clientToken.expiresAt?.let {
                dateFormatISO.parse(it)?.let { expiresAt ->
                    expiresDateFormat.format(expiresAt)
                }
            } ?: "",
            clientToken.reference.orEmpty(),
            retailerName
        )
    }
}

internal class AchAdditionalInfoResolver(
    private val eventDispatcher: EventDispatcher,
    private val paymentResultRepository: PaymentResultRepository,
    private val checkoutErrorEventResolver: BaseErrorEventResolver,
    private val config: PrimerConfig,
    private val completeStripeAchPaymentSessionDelegate: CompleteStripeAchPaymentSessionDelegate,
    private val stripeAchMandateTimestampLoggingDelegate: StripeAchMandateTimestampLoggingDelegate
) : PrimerCheckoutAdditionalInfoResolver {

    override fun resolve(
        clientToken: ClientToken,
        extraParams: AdditionalInfoResolverExtraParams
    ): AchAdditionalInfo.DisplayMandate =
        AchAdditionalInfo.DisplayMandate(
            onAcceptMandate = {
                runSuspendCatching {
                    object {
                        val stripePaymentIntentId = requireNotNullCheck(
                            clientToken.stripePaymentIntentId,
                            StripeIllegalValueKey.MISSING_PAYMENT_INTENT_ID
                        )
                        val completeUrl = requireNotNullCheck(
                            clientToken.sdkCompleteUrl,
                            StripeIllegalValueKey.MISSING_COMPLETION_URL
                        )
                    }
                }.flatMap {
                    val date = Date()
                    stripeAchMandateTimestampLoggingDelegate.logTimestamp(
                        stripePaymentIntentId = it.stripePaymentIntentId,
                        date = date
                    )
                    completeStripeAchPaymentSessionDelegate.invoke(
                        completeUrl = it.completeUrl,
                        paymentMethodId = (
                            extraParams as? AchAdditionalInfoResolverExtraParams
                            )?.paymentMethodId,
                        mandateTimestamp = date
                    )
                }.onSuccess {
                    eventDispatcher.dispatchEvent(
                        CheckoutEvent.PaymentSuccess(
                            PrimerCheckoutData(
                                payment = paymentResultRepository.getPaymentResult().payment
                            )
                        )
                    )
                    eventDispatcher.dispatchEvent(
                        CheckoutEvent.ShowSuccess(
                            successType = when (config.intent.paymentMethodIntent) {
                                PrimerSessionIntent.CHECKOUT -> SuccessType.PAYMENT_SUCCESS
                                PrimerSessionIntent.VAULT -> SuccessType.VAULT_TOKENIZATION_SUCCESS
                            }
                        )
                    )
                }.onFailure(::dispatchCheckoutError)
            },
            onDeclineMandate = {
                dispatchCheckoutError(
                    throwable = PaymentMethodCancelledException(
                        paymentMethodType = PaymentMethodType.STRIPE_ACH.name
                    )
                )
            }
        )

    private fun dispatchCheckoutError(throwable: Throwable) {
        checkoutErrorEventResolver.resolve(
            throwable = throwable,
            type = ErrorMapperType.DEFAULT
        )
    }

    internal data class AchAdditionalInfoResolverExtraParams(
        val paymentMethodId: String
    ) : AdditionalInfoResolverExtraParams
}
