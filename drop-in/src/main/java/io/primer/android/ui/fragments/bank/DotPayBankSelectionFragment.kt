package io.primer.android.ui.fragments.bank

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.primer.android.R
import io.primer.android.databinding.FragmentDotpayBankSelectionBinding
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.fragments.bank.binding.BaseBankSelectionBinding
import io.primer.android.ui.fragments.bank.binding.toBaseBankSelectionBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
internal class DotPayBankSelectionFragment : BaseBankSelectionFragment() {
    private var binding: FragmentDotpayBankSelectionBinding by autoCleaned()

    override val baseBinding: BaseBankSelectionBinding by autoCleaned {
        binding.toBaseBankSelectionBinding()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
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
            if (theme.isDarkMode == true) {
                R.drawable.ic_logo_dotpay_dark
            } else {
                R.drawable.ic_logo_dotpay_light
            },
        )
    }

    companion object {
        fun newInstance() = DotPayBankSelectionFragment()
    }
}
