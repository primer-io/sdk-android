package io.primer.android.ui.fragments.bank

import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.primer.android.R
import io.primer.android.viewmodel.bank.DotPayBankSelectionViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinApiExtension

@ExperimentalCoroutinesApi
@KoinApiExtension
internal class DotPayBankSelectionFragment(
    override val layoutId: Int = R.layout.fragment_dotpay_bank_selection
) : BaseBankSelectionFragment() {

    override val viewModel by viewModel<DotPayBankSelectionViewModel>()

    override fun onBankSelected(issuerId: String) {
        super.onBankSelected(issuerId)
        view?.findViewById<EditText>(R.id.search_bar)?.isEnabled = false
    }

    override fun setupViews(view: View) {
        super.setupViews(view)
        val paymentMethodIcon = view.findViewById<ImageView>(R.id.payment_method_icon)
        paymentMethodIcon.setImageResource(
            if (theme.isDarkMode == true) R.drawable.ic_logo_dotpay_dark
            else R.drawable.ic_logo_dotpay_light
        )
        val searchView = view.findViewById<EditText>(R.id.search_bar)
        searchView.setTextColor(
            theme.input.text.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )

        searchView.doAfterTextChanged { newText ->
            viewModel.onFilterChanged(
                newText.toString()
            )
            view.findViewById<View>(R.id.choose_bank_divider_bottom).visibility =
                when (newText.isNullOrBlank()) {
                    false -> View.INVISIBLE
                    true -> View.VISIBLE
                }
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        primerViewModel.keyboardVisible.observe(viewLifecycleOwner, ::onKeyboardVisibilityChanged)
    }

    private fun onKeyboardVisibilityChanged(visible: Boolean) {
        if (visible) {
            adjustBottomSheetState(BottomSheetBehavior.STATE_EXPANDED)
        }
    }

    companion object {

        fun newInstance() = DotPayBankSelectionFragment()
    }
}
