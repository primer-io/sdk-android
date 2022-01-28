package io.primer.android.ui.fragments.forms.binding

import android.widget.ImageView
import android.widget.TextView
import io.primer.android.databinding.FragmentDynamicFormBinding
import io.primer.android.databinding.FragmentQrCodeBinding

internal class BaseFormBinding(
    val formBackIcon: ImageView,
    val formIcon: ImageView,
    val formTitle: TextView,
    val formDescription: TextView
)

internal fun FragmentDynamicFormBinding.toBaseFormBinding() = BaseFormBinding(
    formBackIcon, formIcon, formTitle, formDescription
)

internal fun FragmentQrCodeBinding.toBaseFormBinding() = BaseFormBinding(
    formBackIcon, formIcon, formTitle, formDescription
)
