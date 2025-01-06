package io.primer.android.ui.fragments.bank

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.primer.android.R
import io.primer.android.core.di.DISdkComponent
import io.primer.android.databinding.FragmentIdealBankSelectionBinding
import io.primer.android.ui.BankSelectionAdapterListener
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.fragments.bank.binding.BaseBankSelectionBinding
import io.primer.android.ui.fragments.bank.binding.toBaseBankSelectionBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
internal class IdealBankSelectionFragment :
    BaseBankSelectionFragment(),
    BankSelectionAdapterListener,
    DISdkComponent {
    private var binding: FragmentIdealBankSelectionBinding by autoCleaned()

    override val baseBinding: BaseBankSelectionBinding by autoCleaned {
        binding.toBaseBankSelectionBinding()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentIdealBankSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupViews() {
        super.setupViews()
        binding.paymentMethodIcon.setImageResource(
            if (theme.isDarkMode == true) {
                R.drawable.ic_logo_ideal_dark
            } else {
                R.drawable.ic_logo_ideal_light
            },
        )
    }

    companion object {
        fun newInstance() = IdealBankSelectionFragment()
    }
}
