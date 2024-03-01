package io.primer.android.payment.klarna

import io.primer.android.R
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.payment.HeadlessDefinition
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.VaultCapability
import io.primer.android.ui.fragments.klarna.KlarnaPaymentCategorySelectionFragment
import io.primer.android.ui.payment.LoadingState
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
internal open class KlarnaDescriptor constructor(
    val options: Klarna,
    localConfig: PrimerConfig,
    config: PaymentMethodConfigDataResponse
) : PaymentMethodDescriptor(config, localConfig) {

    override val selectedBehaviour: SelectedPaymentMethodBehaviour
        get() = NewFragmentBehaviour(
            KlarnaPaymentCategorySelectionFragment::newInstance,
            returnToPreviousOnBack = localConfig.isStandalonePaymentMethod.not()
        )

    override val type: PaymentMethodUiType = PaymentMethodUiType.FORM

    override val vaultCapability = VaultCapability.SINGLE_USE_AND_VAULT

    override fun getLoadingState() = LoadingState(R.drawable.ic_logo_klarna_square)

    override val headlessDefinition: HeadlessDefinition?
        get() = HeadlessDefinition(listOf(PrimerPaymentMethodManagerCategory.NATIVE_UI))
}
