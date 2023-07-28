package io.primer.android.ui.fragments.multibanko

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import io.primer.android.R
import io.primer.android.databinding.FragmentMultibancoConditionsBinding
import io.primer.android.di.DIAppComponent
import io.primer.android.payment.OnActionContinueCallback
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.TokenizationStatus
import io.primer.android.viewmodel.TokenizationViewModel
import io.primer.android.viewmodel.ViewStatus
import org.koin.core.component.inject

internal class MultibancoConditionsFragment : Fragment(), OnActionContinueCallback, DIAppComponent {

    private val theme: PrimerTheme by inject()
    private val primerViewModel: PrimerViewModel by activityViewModels()
    private val tokenizationViewModel: TokenizationViewModel by activityViewModels()

    private var binding: FragmentMultibancoConditionsBinding by autoCleaned()

    private var onActionContinue: (() -> SelectedPaymentMethodBehaviour?)? = null

    override fun onProvideActionContinue(onAction: () -> SelectedPaymentMethodBehaviour?) {
        this.onActionContinue = onAction
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMultibancoConditionsBinding.inflate(
            inflater,
            container,
            false
        )
        setDefaultConfirmButton()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTheme()
        setupListeners()
        setupObservers()
    }

    private fun setupObservers() {
        tokenizationViewModel.tokenizationStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                TokenizationStatus.LOADING -> binding.btnConfirmPay.showProgress()
                TokenizationStatus.ERROR -> binding.btnConfirmPay.hideProgress()
                else -> Unit
            }
        }
    }

    private fun setDefaultConfirmButton() {
        binding.btnConfirmPay.text = getString(R.string.confirmToPayButtonTitle)
    }

    private fun setupTheme() {
        binding.tvTitle.setTextColor(
            theme.titleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )
        binding.tvPoint1.setTextColor(
            theme.subtitleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )
        binding.tvPoint1Description.setTextColor(
            theme.subtitleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )
        binding.tvPoint2.setTextColor(
            theme.subtitleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )
        binding.tvPoint2Description.setTextColor(
            theme.subtitleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )
        binding.tvPoint3.setTextColor(
            theme.subtitleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )
        binding.tvPoint3Description.setTextColor(
            theme.subtitleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )
        binding.ivPaymentMethodIcon.setImageResource(
            if (theme.isDarkMode == true) R.drawable.ic_logo_multibanco_dark
            else R.drawable.ic_logo_multibanco_light
        )
        val imageColorStates = ColorStateList.valueOf(
            theme.titleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )
        binding.ivBack.imageTintList = imageColorStates
    }

    private fun setupListeners() {
        binding.btnConfirmPay.setOnClickListener {
            onActionContinue?.invoke()?.let { behaviour ->
                primerViewModel.executeBehaviour(behaviour)
            }
        }
        binding.ivBack.setOnClickListener {
            goOnSelectedPaymentMethod()
        }
    }

    private fun goOnSelectedPaymentMethod() {
        primerViewModel.viewStatus.value = ViewStatus.SELECT_PAYMENT_METHOD
    }

    companion object {

        fun newInstance() = MultibancoConditionsFragment()
    }
}
