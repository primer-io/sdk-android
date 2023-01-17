package io.primer.android.payment.async.bancontact

import io.primer.android.R
import io.primer.android.components.domain.core.mapper.PrimerPaymentMethodRawDataMapper
import io.primer.android.components.domain.core.mapper.bancontact.BancontactRawCardDataMapper
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.core.models.bancontact.PrimerBancontactCardData
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.deeplink.async.repository.AsyncPaymentMethodDeeplinkRepository
import io.primer.android.payment.HeadlessDefinition
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.async.AsyncPaymentMethod
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.payment.async.CardAsyncPaymentMethodBehaviour
import io.primer.android.ui.fragments.PaymentMethodLoadingFragment
import io.primer.android.ui.fragments.bancontact.BancontactCardFragment
import io.primer.android.ui.payment.LoadingState
import org.koin.core.component.inject

internal class AdyenBancontactCardPaymentMethodDescriptor(
    override val localConfig: PrimerConfig,
    override val options: AsyncPaymentMethod,
    config: PaymentMethodConfigDataResponse,
) : AsyncPaymentMethodDescriptor(localConfig, options, config) {

    private val deeplinkRepository: AsyncPaymentMethodDeeplinkRepository by inject()

    override val type: PaymentMethodUiType = PaymentMethodUiType.FORM

    override val selectedBehaviour: SelectedPaymentMethodBehaviour
        get() = NewFragmentBehaviour(
            BancontactCardFragment::newInstance,
            returnToPreviousOnBack = true
        )

    override val behaviours: List<SelectedPaymentMethodBehaviour> =
        listOf(
            CardAsyncPaymentMethodBehaviour(this),
            NewFragmentBehaviour({ PaymentMethodLoadingFragment.newInstance() })
        )

    override fun getLoadingState() = LoadingState(
        if (localConfig.settings.uiOptions.theme.isDarkMode == true)
            R.drawable.ic_logo_bancontact_dark else R.drawable.ic_logo_bancontact
    )

    override val headlessDefinition: HeadlessDefinition
        get() = HeadlessDefinition(
            listOf(
                PrimerPaymentMethodManagerCategory.RAW_DATA,
                PrimerPaymentMethodManagerCategory.CARD_COMPONENTS
            ),
            HeadlessDefinition.RawDataDefinition(
                PrimerBancontactCardData::class,
                BancontactRawCardDataMapper(deeplinkRepository, config, localConfig.settings)
                    as PrimerPaymentMethodRawDataMapper<PrimerRawData>
            )
        )
}
