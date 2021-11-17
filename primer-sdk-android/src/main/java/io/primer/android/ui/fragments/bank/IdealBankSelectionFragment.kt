package io.primer.android.ui.fragments.bank

import android.view.View
import android.widget.ImageView
import io.primer.android.R
import io.primer.android.di.DIAppComponent
import org.koin.android.viewmodel.ext.android.viewModel

import io.primer.android.ui.BankSelectionAdapterListener
import io.primer.android.viewmodel.bank.BankSelectionViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.component.KoinApiExtension

@ExperimentalCoroutinesApi
@KoinApiExtension
internal class IdealBankSelectionFragment(
    override val layoutId: Int = R.layout.fragment_ideal_bank_selection
) : BaseBankSelectionFragment(),
    BankSelectionAdapterListener,
    DIAppComponent {

    override val viewModel by viewModel<BankSelectionViewModel>()

    override fun setupViews(view: View) {
        super.setupViews(view)
        val paymentMethodIcon = view.findViewById<ImageView>(R.id.payment_method_icon)
        paymentMethodIcon.setColorFilter(
            theme.titleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )
    }

    companion object {

        fun newInstance() = IdealBankSelectionFragment()
    }
}
