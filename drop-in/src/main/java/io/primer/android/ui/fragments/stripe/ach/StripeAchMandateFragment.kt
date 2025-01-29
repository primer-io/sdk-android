package io.primer.android.ui.fragments.stripe.ach

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentDialog
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.commitNow
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.primer.android.AchMandateActionHandler
import io.primer.android.R
import io.primer.android.core.di.extensions.inject
import io.primer.android.databinding.PrimerFragmentStripeAchMandateBinding
import io.primer.android.errors.data.exception.PaymentMethodCancelledException
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.stripe.ach.api.mandate.delegate.GetStripeMandateDelegate
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.fragments.base.BaseFragment
import io.primer.android.ui.settings.PrimerTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

internal class StripeAchMandateFragment : BaseFragment() {
    private var binding: PrimerFragmentStripeAchMandateBinding by autoCleaned()

    private val primerTheme: PrimerTheme by inject()
    private val errorMapperRegistry: ErrorMapperRegistry by inject()
    private val getStripeMandateDelegate: GetStripeMandateDelegate by inject()
    private val errorHandler: CheckoutErrorHandler by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding =
            PrimerFragmentStripeAchMandateBinding.inflate(
                inflater,
                container,
                false,
            )
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        getParentDialogOrNull()?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            lifecycleScope.launch {
                popBackStack()
                errorHandler.handle(
                    error =
                    errorMapperRegistry.getPrimerError(
                        PaymentMethodCancelledException(PaymentMethodType.STRIPE_ACH.name),
                    ),
                    payment = null,
                )
            }
        }

        getToolbar()?.apply {
            getBackButton().isVisible = false
            showOnlyTitle(R.string.pay_with_ach)
        }

        getStripeMandateDelegate()
            .onSuccess { binding.mandate.text = it }
            .onFailure { Log.e(TAG, "Failed to fetch mandate", it) }

        binding.accept.setTheme(primerTheme)
        binding.accept.text = getString(R.string.stripe_ach_mandate_accept_button)
        binding.accept.setOnClickListener {
            binding.accept.showProgress()
            submit(isAccepted = true)
        }
        binding.decline.setOnClickListener {
            submit(isAccepted = false)
        }

        adjustBottomSheetState(BottomSheetBehavior.STATE_EXPANDED)
    }

    private fun submit(isAccepted: Boolean) {
        binding.accept.isEnabled = false
        binding.decline.isEnabled = false
        lifecycleScope.launch {
            (requireActivity() as? AchMandateActionHandler)?.let {
                it.handleAchMandateAction(isAccepted)
                yield()
                popBackStack()
            }
        }
    }

    private fun getParentDialogOrNull() = ((parentFragment as? DialogFragment)?.dialog as? ComponentDialog)

    private fun popBackStack() {
        parentFragmentManager.popBackStack()
        parentFragmentManager.commitNow { remove(this@StripeAchMandateFragment) }
    }

    companion object {
        private val TAG = StripeAchMandateFragment::class.simpleName

        fun newInstance() = StripeAchMandateFragment()
    }
}
