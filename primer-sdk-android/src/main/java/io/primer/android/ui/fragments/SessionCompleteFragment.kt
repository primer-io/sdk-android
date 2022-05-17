package io.primer.android.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.R
import io.primer.android.analytics.data.models.AnalyticsAction
import io.primer.android.analytics.data.models.ObjectType
import io.primer.android.analytics.data.models.Place
import io.primer.android.analytics.domain.models.UIAnalyticsParams
import io.primer.android.databinding.FragmentSessionCompleteBinding
import io.primer.android.di.DIAppComponent
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.model.CheckoutExitReason
import io.primer.android.presentation.base.BaseViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import io.primer.android.ui.extensions.autoCleaned
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

private const val SESSION_COMPLETE_DISMISS_DELAY_KEY = "SUCCESS_FRAGMENT_DISMISS_DELAY"
private const val SESSION_COMPLETE_DISMISS_DELAY_DEFAULT = 3000L
private const val SESSION_COMPLETE_MESSAGE_KEY = "SESSION_COMPLETE_MESSAGE"
private const val SESSION_COMPLETE_CUSTOM_MESSAGE_KEY = "SESSION_COMPLETE_CUSTOM_MESSAGE"
private const val SESSION_COMPLETE_IS_ERROR_KEY = "IS_ERROR"

enum class SuccessType {
    DEFAULT, VAULT_TOKENIZATION_SUCCESS, PAYMENT_SUCCESS
}

enum class ErrorType {
    DEFAULT, VAULT_TOKENIZATION_FAILED, PAYMENT_FAILED
}

sealed class SessionCompleteViewType {
    class Success(val successType: SuccessType) : SessionCompleteViewType()
    class Error(val errorType: ErrorType, val message: String?) : SessionCompleteViewType()
}

@KoinApiExtension
class SessionCompleteFragment : Fragment(), DIAppComponent {

    private val theme: PrimerTheme by inject()

    private val viewModel: BaseViewModel by viewModel()
    private var binding: FragmentSessionCompleteBinding by autoCleaned()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSessionCompleteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val place =
            if (arguments?.getBoolean(SESSION_COMPLETE_IS_ERROR_KEY) == true) Place.ERROR_SCREEN
            else Place.SUCCESS_SCREEN

        viewModel.addAnalyticsEvent(
            UIAnalyticsParams(
                AnalyticsAction.VIEW,
                ObjectType.VIEW,
                place
            )
        )

        binding.sessionCompleteMessage.text = arguments?.getString(
            SESSION_COMPLETE_CUSTOM_MESSAGE_KEY
        ) ?: arguments?.getInt(SESSION_COMPLETE_MESSAGE_KEY)?.let {
            getString(it)
        }

        val textColor = theme.titleText.defaultColor.getColor(requireContext(), theme.isDarkMode)
        binding.sessionCompleteMessage.setTextColor(textColor)

        if (arguments?.getBoolean(SESSION_COMPLETE_IS_ERROR_KEY) == true) {
            binding.sessionCompleteIcon.setImageResource(R.drawable.ic_error)
        }

        Handler(Looper.getMainLooper()).postDelayed(
            {
                EventBus.broadcast(CheckoutEvent.DismissInternal(CheckoutExitReason.EXIT_SUCCESS))
            },
            arguments?.getLong(SESSION_COMPLETE_DISMISS_DELAY_KEY)
                ?: SESSION_COMPLETE_DISMISS_DELAY_DEFAULT
        )
    }

    companion object {

        private fun getSuccessMessage(successType: SuccessType): Int {
            return when (successType) {
                SuccessType.DEFAULT -> R.string.success_text
                SuccessType.VAULT_TOKENIZATION_SUCCESS -> R.string.payment_method_added_message
                SuccessType.PAYMENT_SUCCESS -> R.string.payment_request_completed_successfully
            }
        }

        private fun getErrorMessage(errorType: ErrorType): Int {
            return when (errorType) {
                ErrorType.DEFAULT -> R.string.error_default
                ErrorType.VAULT_TOKENIZATION_FAILED -> R.string.payment_method_not_added_message
                ErrorType.PAYMENT_FAILED -> R.string.payment_request_unsuccessful
            }
        }

        fun newInstance(delay: Int, viewType: SessionCompleteViewType): SessionCompleteFragment {
            return SessionCompleteFragment().apply {
                arguments = Bundle().apply {
                    when (viewType) {
                        is SessionCompleteViewType.Error -> {
                            putBoolean(SESSION_COMPLETE_IS_ERROR_KEY, true)
                            putInt(
                                SESSION_COMPLETE_MESSAGE_KEY,
                                getErrorMessage(viewType.errorType),
                            )
                            putString(SESSION_COMPLETE_CUSTOM_MESSAGE_KEY, viewType.message)
                        }
                        is SessionCompleteViewType.Success -> {
                            putBoolean(SESSION_COMPLETE_IS_ERROR_KEY, false)
                            putInt(
                                SESSION_COMPLETE_MESSAGE_KEY,
                                getSuccessMessage(viewType.successType),
                            )
                        }
                    }
                    putLong(SESSION_COMPLETE_DISMISS_DELAY_KEY, delay.toLong())
                }
            }
        }
    }
}
