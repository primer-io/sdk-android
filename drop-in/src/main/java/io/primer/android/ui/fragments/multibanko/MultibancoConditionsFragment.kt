package io.primer.android.ui.fragments.multibanko

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import io.primer.android.PrimerSessionIntent
import io.primer.android.R
import io.primer.android.components.manager.nativeUi.PrimerHeadlessUniversalCheckoutNativeUiManager
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.inject
import io.primer.android.core.extensions.getSerializableExtraCompat
import io.primer.android.databinding.FragmentMultibancoConditionsBinding
import io.primer.android.di.extension.activityViewModel
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.PrimerViewModelFactory
import io.primer.android.viewmodel.ViewStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
internal class MultibancoConditionsFragment : Fragment(), DISdkComponent {
    private val theme: PrimerTheme by inject()
    private val primerViewModel: PrimerViewModel
        by activityViewModel<PrimerViewModel, PrimerViewModelFactory>()

    private var binding: FragmentMultibancoConditionsBinding by autoCleaned()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding =
            FragmentMultibancoConditionsBinding.inflate(
                inflater,
                container,
                false,
            )
        setDefaultConfirmButton()
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupTheme()
        setupListeners()
    }

    private fun setDefaultConfirmButton() {
        binding.btnConfirmPay.text = getString(R.string.confirmToPayButtonTitle)
    }

    private fun setupTheme() {
        binding.tvTitle.setTextColor(
            theme.titleText.defaultColor.getColor(requireContext(), theme.isDarkMode),
        )
        val subtitleTextColor = theme.subtitleText.defaultColor.getColor(requireContext(), theme.isDarkMode)
        binding.tvPoint1.setTextColor(subtitleTextColor)
        binding.tvPoint1Description.setTextColor(subtitleTextColor)
        binding.tvPoint2.setTextColor(subtitleTextColor)
        binding.tvPoint2Description.setTextColor(subtitleTextColor)
        binding.tvPoint3.setTextColor(subtitleTextColor)
        binding.tvPoint3Description.setTextColor(subtitleTextColor)
        binding.ivPaymentMethodIcon.setImageResource(
            if (theme.isDarkMode == true) {
                R.drawable.ic_logo_multibanco_dark
            } else {
                R.drawable.ic_logo_multibanco_light
            },
        )
        val imageColorStates =
            ColorStateList.valueOf(
                theme.titleText.defaultColor.getColor(
                    requireContext(),
                    theme.isDarkMode,
                ),
            )
        binding.ivBack.imageTintList = imageColorStates
    }

    private fun setupListeners() {
        binding.btnConfirmPay.setOnClickListener {
            binding.btnConfirmPay.showProgress()

            val paymentMethodType = requireArguments().getString(PAYMENT_METHOD_TYPE_KEY)
            val sessionIntent = requireArguments().getSerializableExtraCompat<PrimerSessionIntent>(SESSION_INTENT_KEY)

            if (paymentMethodType == null || sessionIntent == null) {
                Log.e(MultibancoPaymentFragment::class.simpleName, "Expected 'paymentMethodType' and 'sessionIntent'")
                return@setOnClickListener
            }

            PrimerHeadlessUniversalCheckoutNativeUiManager.newInstance(paymentMethodType)
                .also { primerViewModel.addCloseable { it.cleanup() } }
                .also { it.showPaymentMethod(requireContext(), sessionIntent = sessionIntent) }
        }
        binding.ivBack.setOnClickListener {
            goOnSelectedPaymentMethod()
        }
    }

    private fun goOnSelectedPaymentMethod() {
        primerViewModel.setViewStatus(ViewStatus.SelectPaymentMethod)
    }

    companion object {
        private const val SESSION_INTENT_KEY = "SESSION_INTENT"
        private const val PAYMENT_METHOD_TYPE_KEY = "PAYMENT_METHOD_TYPE"

        fun newInstance(
            sessionIntent: PrimerSessionIntent,
            paymentMethodType: String,
        ) = MultibancoConditionsFragment().apply {
            arguments =
                bundleOf(
                    SESSION_INTENT_KEY to sessionIntent,
                    PAYMENT_METHOD_TYPE_KEY to paymentMethodType,
                )
        }
    }
}
