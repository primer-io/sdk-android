package io.primer.android.ui.fragments.forms.binding

import android.widget.TextView
import io.primer.android.databinding.PrimerFragmentDynamicFormBinding
import io.primer.android.databinding.PrimerFragmentMultibancoPaymentBinding
import io.primer.android.databinding.PrimerFragmentQrCodeBinding

internal class BaseFormBinding(
    val formTitle: TextView,
    val formDescription: TextView,
)

internal fun PrimerFragmentDynamicFormBinding.toBaseFormBinding() =
    BaseFormBinding(
        formTitle,
        formDescription,
    )

internal fun PrimerFragmentQrCodeBinding.toBaseFormBinding() =
    BaseFormBinding(
        tvTitleComplete,
        tvDescription,
    )

internal fun PrimerFragmentMultibancoPaymentBinding.toBaseFormBinding() =
    BaseFormBinding(
        tvTitleComplete,
        tvDescription,
    )
