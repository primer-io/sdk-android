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
import io.primer.android.ui.components.SearchViewWidget

@Suppress("complexity:LongParameterList")
internal class BaseBankSelectionBinding(
    val chooseBankTitle: TextView,
    val paymentMethodBack: ImageView,
    val searchBar: SearchViewWidget,
    val progressBar: ProgressBar,
    val chooseBankParent: RelativeLayout,
    val errorLayout: ErrorLoadingLayoutBinding,
    val spacer: Space,
    val recyclerView: RecyclerView,
    val chooseBankDividerBottom: View
)

internal fun FragmentIdealBankSelectionBinding.toBaseBankSelectionBinding() =
    BaseBankSelectionBinding(
        chooseBankTitle = chooseBankTitle,
        paymentMethodBack = paymentMethodBack,
        searchBar = searchBar,
        progressBar = progressBar,
        chooseBankParent = chooseBankParent,
        errorLayout = errorLayout,
        spacer = spacer,
        recyclerView = banksList,
        chooseBankDividerBottom = chooseBankDividerBottom
    )

internal fun FragmentDotpayBankSelectionBinding.toBaseBankSelectionBinding() =
    BaseBankSelectionBinding(
        chooseBankTitle = chooseBankTitle,
        paymentMethodBack = paymentMethodBack,
        searchBar = searchBar,
        progressBar = progressBar,
        chooseBankParent = chooseBankParent,
        errorLayout = errorLayout,
        spacer = spacer,
        recyclerView = banksList,
        chooseBankDividerBottom = chooseBankDividerBottom
    )
