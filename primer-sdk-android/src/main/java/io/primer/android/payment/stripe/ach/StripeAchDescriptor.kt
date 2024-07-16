package io.primer.android.payment.stripe.ach

import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.CompleteStripeAchPaymentSessionDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.StripeAchMandateTimestampLoggingDelegate
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.payments.additionalInfo.AchAdditionalInfoResolver
import io.primer.android.domain.payments.additionalInfo.PrimerCheckoutAdditionalInfoResolver
import io.primer.android.domain.payments.create.repository.PaymentResultRepository
import io.primer.android.events.EventDispatcher
import io.primer.android.payment.HeadlessDefinition
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.VaultCapability
import io.primer.android.ui.fragments.stripe.ach.StripeAchUserDetailsCollectionFragment

@Suppress("LongParameterList")
internal class StripeAchDescriptor(
    localConfig: PrimerConfig,
    config: PaymentMethodConfigDataResponse,
    eventDispatcher: EventDispatcher,
    paymentResultRepository: PaymentResultRepository,
    checkoutErrorEventResolver: BaseErrorEventResolver,
    completeStripeAchPaymentSessionDelegate: CompleteStripeAchPaymentSessionDelegate,
    stripeAchMandateTimestampLoggingDelegate: StripeAchMandateTimestampLoggingDelegate
) : PaymentMethodDescriptor(config, localConfig) {

    override val selectedBehaviour: SelectedPaymentMethodBehaviour
        get() = NewFragmentBehaviour(
            StripeAchUserDetailsCollectionFragment::newInstance,
            returnToPreviousOnBack = localConfig.isStandalonePaymentMethod.not()
        )

    override val type: PaymentMethodUiType = PaymentMethodUiType.FORM

    override val additionalInfoResolver: PrimerCheckoutAdditionalInfoResolver =
        AchAdditionalInfoResolver(
            eventDispatcher = eventDispatcher,
            paymentResultRepository = paymentResultRepository,
            checkoutErrorEventResolver = checkoutErrorEventResolver,
            config = localConfig,
            completeStripeAchPaymentSessionDelegate = completeStripeAchPaymentSessionDelegate,
            stripeAchMandateTimestampLoggingDelegate = stripeAchMandateTimestampLoggingDelegate
        )

    override val vaultCapability: VaultCapability = VaultCapability.SINGLE_USE_ONLY

    override val headlessDefinition: HeadlessDefinition = HeadlessDefinition(
        listOf(
            PrimerPaymentMethodManagerCategory.NATIVE_UI
        )
    )
}
