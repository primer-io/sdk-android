package io.primer.android.ui.fragments.bank

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.primer.android.R
import io.primer.android.databinding.FragmentIdealBankSelectionBinding
import io.primer.android.di.DIAppComponent
import org.koin.android.viewmodel.ext.android.viewModel

import io.primer.android.ui.BankSelectionAdapterListener
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.fragments.bank.binding.BaseBankSelectionBinding
import io.primer.android.ui.fragments.bank.binding.toBaseBankSelectionBinding
import io.primer.android.viewmodel.bank.BankSelectionViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.component.KoinApiExtension

@ExperimentalCoroutinesApi
@KoinApiExtension
internal class IdealBankSelectionFragment :
    BaseBankSelectionFragment(),
    BankSelectionAdapterListener,
    DIAppComponent {

    private var binding: FragmentIdealBankSelectionBinding by autoCleaned()

    override val baseBinding: BaseBankSelectionBinding by autoCleaned {
        binding.toBaseBankSelectionBinding()
    }

    override val viewModel by viewModel<BankSelectionViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIdealBankSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupViews() {
        super.setupViews()
        binding.paymentMethodIcon.setImageResource(
            if (theme.isDarkMode == true) R.drawable.ic_logo_ideal_dark
            else R.drawable.ic_logo_ideal_light
        )
    }

    companion object {

        fun newInstance() = IdealBankSelectionFragment()
    }
}
