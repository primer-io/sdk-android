package io.primer.android.ui.fragments.stripe.ach

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentDialog
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.commit
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.GetStripeMandateDelegate
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.exception.PaymentMethodCancelledException
import io.primer.android.databinding.FragmentStripeAchMandateBinding
import io.primer.android.di.extension.inject
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.fragments.base.BaseFragment

internal class StripeAchMandateFragment : BaseFragment() {
    private var binding: FragmentStripeAchMandateBinding by autoCleaned()

    private val eventResolver: BaseErrorEventResolver by inject()
    private val getStripeMandateDelegate: GetStripeMandateDelegate by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStripeAchMandateBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getParentDialogOrNull()?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            eventResolver.resolve(
                PaymentMethodCancelledException(PaymentMethodType.STRIPE_ACH.name),
                ErrorMapperType.DEFAULT
            )
        }

        getStripeMandateDelegate.invoke()
            .onSuccess { binding.mandate.text = it }
            .onFailure { Log.e(TAG, "Failed to fetch mandate", it) }

        binding.accept.setOnClickListener {
            submit(isAccepted = true)
        }
        binding.decline.setOnClickListener {
            submit(isAccepted = false)
        }

        adjustBottomSheetState(BottomSheetBehavior.STATE_EXPANDED)
    }

    private fun submit(isAccepted: Boolean) {
        setFragmentResult(RESULT_KEY, bundleOf(IS_ACCEPTED to isAccepted))
        popBackStack()
    }

    private fun getParentDialogOrNull() = ((parentFragment as? DialogFragment)?.dialog as? ComponentDialog)

    private fun popBackStack() {
        parentFragmentManager.popBackStack()
        parentFragmentManager.commit { remove(this@StripeAchMandateFragment) }
    }

    companion object {
        private val TAG = StripeAchMandateFragment::class.simpleName

        const val RESULT_KEY = "mandate_result"
        const val IS_ACCEPTED = "is_accepted"

        fun newInstance() = StripeAchMandateFragment()
    }
}
