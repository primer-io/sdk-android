package io.primer.android.ui.fragments.bank

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.primer.android.R
import io.primer.android.databinding.FragmentDotpayBankSelectionBinding
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.fragments.bank.binding.BaseBankSelectionBinding
import io.primer.android.ui.fragments.bank.binding.toBaseBankSelectionBinding
import io.primer.android.viewmodel.bank.DotPayBankSelectionViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinApiExtension

@ExperimentalCoroutinesApi
@KoinApiExtension
internal class DotPayBankSelectionFragment : BaseBankSelectionFragment() {

    private var binding: FragmentDotpayBankSelectionBinding by autoCleaned()

    override val baseBinding: BaseBankSelectionBinding by autoCleaned {
        binding.toBaseBankSelectionBinding()
    }

    override val viewModel by viewModel<DotPayBankSelectionViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDotpayBankSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onBankSelected(issuerId: String) {
        super.onBankSelected(issuerId)
        binding.searchBar.isEnabled = false
    }

    override fun setupViews() {
        super.setupViews()
        binding.paymentMethodIcon.setImageResource(
            if (theme.isDarkMode == true) R.drawable.ic_logo_dotpay_dark
            else R.drawable.ic_logo_dotpay_light
        )
        binding.searchBar.setTextColor(
            theme.input.text.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )

        binding.searchBar.doAfterTextChanged { newText ->
            viewModel.onFilterChanged(
                newText.toString()
            )
            binding.chooseBankDividerBottom.visibility =
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
