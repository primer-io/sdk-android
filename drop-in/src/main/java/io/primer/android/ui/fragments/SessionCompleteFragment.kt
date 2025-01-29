@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.ui.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commitNow
import androidx.lifecycle.lifecycleScope
import io.primer.android.R
import io.primer.android.analytics.data.models.AnalyticsAction
import io.primer.android.analytics.data.models.ObjectType
import io.primer.android.analytics.data.models.Place
import io.primer.android.analytics.domain.models.UIAnalyticsParams
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.extensions.getSerializableExtraCompat
import io.primer.android.databinding.PrimerFragmentSessionCompleteBinding
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.extensions.getParentDialogOrNull
import io.primer.android.ui.extensions.setTheme
import io.primer.android.ui.fragments.base.BaseFragment
import io.primer.android.ui.utils.toPx
import io.primer.android.viewmodel.ViewStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.io.Serializable

private const val SESSION_COMPLETE_DISMISS_DELAY_KEY = "SUCCESS_FRAGMENT_DISMISS_DELAY"
private const val SESSION_COMPLETE_DISMISS_DELAY_DEFAULT = 3000L
private const val SESSION_COMPLETE_VIEW_TYPE = "SESSION_COMPLETE_VIEW_TYPE"

enum class SuccessType {
    DEFAULT,
    VAULT_TOKENIZATION_SUCCESS,
    PAYMENT_SUCCESS,
}

enum class ErrorType {
    DEFAULT,
    VAULT_TOKENIZATION_FAILED,
    PAYMENT_FAILED,
    PAYMENT_CANCELLED,
}

@Suppress("SerialVersionUIDInSerializableClass")
sealed class SessionCompleteViewType : Serializable {
    data class Success(val successType: SuccessType) : SessionCompleteViewType()

    data class Error(val errorType: ErrorType, val message: String?) : SessionCompleteViewType()
}

private const val VIEW_HEIGHT_DP = 300

internal class SessionCompleteFragment : BaseFragment(), DISdkComponent {
    private val isVaultedPaymentFlow get() = primerViewModel.selectedPaymentMethod.value == null

    private val isStandalonePaymentMethod
        get() =
            primerViewModel.selectedPaymentMethod.value?.uiOptions?.isStandalonePaymentMethod == true
    private var binding: PrimerFragmentSessionCompleteBinding by autoCleaned()

    private val selectedPaymentMethodType by lazy {
        primerViewModel.selectedPaymentMethod.value?.paymentMethodType
            ?: primerViewModel.selectedSavedPaymentMethod?.paymentMethodType
    }
    private val isStripeAchPaymentMethod by lazy {
        selectedPaymentMethodType == PaymentMethodType.STRIPE_ACH.name
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = PrimerFragmentSessionCompleteBinding.inflate(inflater, container, false)
        return binding.root
    }

    @Suppress("LongMethod", "CyclomaticComplexMethod")
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        applyStyle()
        addOnBackPressedCallback()
        updateRootHeight()

        val viewType = arguments?.getSerializableExtraCompat<SessionCompleteViewType>(SESSION_COMPLETE_VIEW_TYPE)
        val isError = viewType is SessionCompleteViewType.Error
        val isCancellationError =
            viewType is SessionCompleteViewType.Error &&
                viewType.errorType == ErrorType.PAYMENT_CANCELLED

        primerViewModel.addAnalyticsEvent(
            UIAnalyticsParams(
                action = AnalyticsAction.VIEW,
                objectType = ObjectType.VIEW,
                place = if (isError) Place.ERROR_SCREEN else Place.SUCCESS_SCREEN,
            ),
        )

        if (isStripeAchPaymentMethod) {
            getToolbar()?.apply {
                showOnlyTitle(R.string.pay_with_ach)
                getBackButton().isVisible = false
            }
            binding.sessionCompleteMessage.text =
                getString(
                    if (isError) {
                        if (isCancellationError) {
                            R.string.session_complete_payment_cancellation_title
                        } else {
                            R.string.session_complete_payment_failure_title
                        }
                    } else {
                        R.string.session_complete_payment_success_title
                    },
                )
            binding.sessionCompleteDescription.text = viewType.getCompletedMessageOrNull()
            if (isError) {
                if (isCancellationError || isVaultedPaymentFlow) {
                    binding.primaryButton.text = getString(R.string.choose_other_payment_method_button)
                    binding.primaryButton.setOnClickListener { popBackStack() }
                    binding.primaryButton.isVisible = !isStandalonePaymentMethod
                    binding.secondaryButton.isVisible = false
                } else {
                    with(binding.primaryButton) {
                        isVisible = true
                        text = getString(R.string.retry_button)
                        setOnClickListener { restart() }
                    }
                    with(binding.secondaryButton) {
                        isVisible = !isStandalonePaymentMethod
                        if (isVisible) {
                            text = getString(R.string.choose_other_payment_method_button)
                            setOnClickListener { popBackStack() }
                        }
                    }
                }
            } else {
                binding.primaryButton.isVisible = false
                binding.secondaryButton.isVisible = false
            }
        } else {
            binding.sessionCompleteDescription.isVisible = false
            binding.sessionCompleteMessage.text = viewType.getCompletedMessageOrNull()
            binding.primaryButton.isVisible = false
            binding.secondaryButton.isVisible = false
        }

        binding.sessionCompleteIcon.setImageResource(
            if (isError) {
                R.drawable.ic_error
            } else {
                R.drawable.ic_check_success
            },
        )

        if (!binding.primaryButton.isVisible && !binding.secondaryButton.isVisible) {
            Handler(Looper.getMainLooper()).postDelayed(
                { primerViewModel.setViewStatus(ViewStatus.Dismiss) },
                arguments?.getLong(SESSION_COMPLETE_DISMISS_DELAY_KEY, SESSION_COMPLETE_DISMISS_DELAY_DEFAULT)
                    ?: SESSION_COMPLETE_DISMISS_DELAY_DEFAULT,
            )
        }
    }

    private fun applyStyle() {
        val color =
            ColorStateList.valueOf(
                theme.titleText.defaultColor.getColor(
                    context = requireContext(),
                    isDarkMode = theme.isDarkMode,
                ),
            )
        binding.sessionCompleteMessage.setTextColor(color)
        binding.primaryButton.setTheme(theme)
    }

    private fun addOnBackPressedCallback() {
        getParentDialogOrNull()?.onBackPressedDispatcher?.addCallback(this) {
            viewLifecycleOwner.lifecycleScope.launch {
                primerViewModel.setViewStatus(ViewStatus.Dismiss)
            }
        }
    }

    private fun restart() {
        primerViewModel.selectedPaymentMethod.value?.let { paymentMethod ->
            popBackStack()
            primerViewModel.selectPaymentMethod(paymentMethod)
        }
    }

    // region Utils
    private fun popBackStack() {
        parentFragmentManager.popBackStackImmediate(
            SelectPaymentMethodFragment.TAG,
            FragmentManager.POP_BACK_STACK_INCLUSIVE,
        )
        runCatching {
            parentFragmentManager.commitNow(allowStateLoss = true) {
                /*
                Manually remove this fragment as it is not added to the backstack,
                therefore it won't get removed automatically on pop.
                 */
                remove(this@SessionCompleteFragment)
            }
        }
        primerViewModel.goToSelectPaymentMethodsView()
    }

    private fun updateRootHeight() {
        binding.root.updateLayoutParams<ViewGroup.LayoutParams> {
            height =
                if (isStripeAchPaymentMethod) {
                    ViewGroup.LayoutParams.WRAP_CONTENT
                } else {
                    requireContext().toPx(VIEW_HEIGHT_DP).toInt()
                }
        }
    }

    private fun SessionCompleteViewType?.getCompletedMessageOrNull(): String? =
        when (this) {
            is SessionCompleteViewType.Error -> {
                message ?: getString(getErrorMessage(errorType, isStripeAchPaymentMethod))
            }

            is SessionCompleteViewType.Success -> {
                getString(getSuccessMessage(successType, isStripeAchPaymentMethod))
            }

            null -> null
        }
    // endregion

    companion object {
        private val TAG = SessionCompleteFragment::class.simpleName

        private fun getSuccessMessage(
            successType: SuccessType,
            isStripeAch: Boolean = false,
        ): Int {
            return when (successType) {
                SuccessType.DEFAULT -> R.string.success_text
                SuccessType.VAULT_TOKENIZATION_SUCCESS -> R.string.payment_method_added_message
                SuccessType.PAYMENT_SUCCESS -> getSuccessStringRes(isStripeAch = isStripeAch)
            }
        }

        private fun getSuccessStringRes(isStripeAch: Boolean) =
            if (isStripeAch) {
                R.string.stripe_ach_payment_request_completed_successfully
            } else {
                R.string.payment_request_completed_successfully
            }

        private fun getErrorMessage(
            errorType: ErrorType,
            isStripeAch: Boolean = false,
        ): Int {
            return when (errorType) {
                ErrorType.DEFAULT -> R.string.error_default
                ErrorType.VAULT_TOKENIZATION_FAILED -> R.string.payment_method_not_added_message
                ErrorType.PAYMENT_FAILED -> R.string.payment_request_unsuccessful
                ErrorType.PAYMENT_CANCELLED -> getCancellationStringRes(isStripeAch = isStripeAch)
            }
        }

        private fun getCancellationStringRes(isStripeAch: Boolean) =
            if (isStripeAch) {
                R.string.stripe_ach_payment_request_cancelled
            } else {
                R.string.payment_request_unsuccessful
            }

        fun newInstance(
            delay: Int,
            viewType: SessionCompleteViewType,
        ): SessionCompleteFragment {
            return SessionCompleteFragment().apply {
                arguments =
                    Bundle().apply {
                        putSerializable(SESSION_COMPLETE_VIEW_TYPE, viewType)
                        putLong(SESSION_COMPLETE_DISMISS_DELAY_KEY, delay.toLong())
                    }
            }
        }
    }
}
