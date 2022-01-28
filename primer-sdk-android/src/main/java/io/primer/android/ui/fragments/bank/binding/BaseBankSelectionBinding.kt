package io.primer.android.ui.fragments.bank.binding

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Space
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.primer.android.databinding.ErrorLoadingLayoutBinding
import io.primer.android.databinding.FragmentDotpayBankSelectionBinding
import io.primer.android.databinding.FragmentIdealBankSelectionBinding

internal class BaseBankSelectionBinding(
    val chooseBankTitle: TextView,
    val paymentMethodBack: ImageView,
    val progressBar: ProgressBar,
    val chooseBankParent: RelativeLayout,
    val errorLayout: ErrorLoadingLayoutBinding,
    val spacer: Space,
    val recyclerView: RecyclerView,
    val chooseBankDividerBottom: View
)

internal fun FragmentIdealBankSelectionBinding.toBaseBankSelectionBinding() =
    BaseBankSelectionBinding(
        chooseBankTitle,
        paymentMethodBack,
        progressBar,
        chooseBankParent,
        errorLayout,
        spacer,
        chooseBankRecyclerView,
        chooseBankDividerBottom
    )

internal fun FragmentDotpayBankSelectionBinding.toBaseBankSelectionBinding() =
    BaseBankSelectionBinding(
        chooseBankTitle,
        paymentMethodBack,
        progressBar,
        chooseBankParent,
        errorLayout,
        spacer,
        chooseBankRecyclerView,
        chooseBankDividerBottom
    )
