package io.primer.android.payment.async.blik

import io.primer.android.R
import io.primer.android.components.domain.core.mapper.PrimerPaymentMethodRawDataMapper
import io.primer.android.components.domain.core.mapper.otp.OtpRawDataMapper
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.core.models.otp.PrimerOtpCodeData
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.deeplink.async.repository.AsyncPaymentMethodDeeplinkRepository
import io.primer.android.payment.HeadlessDefinition
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SDKCapability
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.async.AsyncPaymentMethod
import io.primer.android.payment.async.AsyncPaymentMethodBehaviour
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.ui.fragments.forms.DynamicFormFragment
import io.primer.android.ui.payment.LoadingState
import org.koin.core.component.inject

internal class AdyenBlikPaymentMethodDescriptor(
    override val options: AsyncPaymentMethod,
    localConfig: PrimerConfig,
    config: PaymentMethodConfigDataResponse,
) : AsyncPaymentMethodDescriptor(options, localConfig, config) {

    private val deeplinkRepository: AsyncPaymentMethodDeeplinkRepository by inject()

    override val selectedBehaviour =
        NewFragmentBehaviour(
            DynamicFormFragment::newInstance,
            returnToPreviousOnBack = localConfig.isStandalonePaymentMethod.not()
        )

    override fun getLoadingState() = LoadingState(
        R.drawable.ic_logo_blik_square,
        R.string.payment_method_blik_loading_placeholder
    )

    override val type: PaymentMethodUiType = PaymentMethodUiType.FORM

    override val sdkCapabilities: List<SDKCapability> =
        listOf(SDKCapability.DROP_IN, SDKCapability.HEADLESS)

    override val behaviours: List<SelectedPaymentMethodBehaviour> =
        listOf(AsyncPaymentMethodBehaviour(this))

    override val headlessDefinition: HeadlessDefinition
        get() = HeadlessDefinition(
            listOf(PrimerPaymentMethodManagerCategory.RAW_DATA),
            HeadlessDefinition.RawDataDefinition(
                PrimerOtpCodeData::class,
                OtpRawDataMapper(deeplinkRepository, config, localConfig.settings)
                    as PrimerPaymentMethodRawDataMapper<PrimerRawData>,
            )
        )
}
