package io.primer.android.ui.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import io.primer.android.R
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.model.dto.CheckoutExitReason

private const val SESSION_COMPLETE_DISMISS_DELAY_KEY = "SUCCESS_FRAGMENT_DISMISS_DELAY"
private const val SESSION_COMPLETE_DISMISS_DELAY_DEFAULT = 3000L
private const val SESSION_COMPLETE_MESSAGE_KEY = "SESSION_COMPLETE_MESSAGE"
private const val SESSION_COMPLETE_IS_ERROR_KEY = "IS_ERROR"

enum class SuccessType {
    DEFAULT, VAULT_TOKENIZATION_SUCCESS, PAYMENT_SUCCESS
}

enum class ErrorType {
    DEFAULT, VAULT_TOKENIZATION_FAILED, PAYMENT_FAILED
}

sealed class SessionCompleteViewType {
    class Success(val successType: SuccessType) : SessionCompleteViewType()
    class Error(val errorType: ErrorType) : SessionCompleteViewType()
}

class SessionCompleteFragment : Fragment() {

    private var delay: Long? = null
    private var isError: Boolean = false
    private var message: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            delay = it.getLong(SESSION_COMPLETE_DISMISS_DELAY_KEY)
            isError = it.getBoolean(SESSION_COMPLETE_IS_ERROR_KEY)
            message = it.getString(SESSION_COMPLETE_MESSAGE_KEY)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_session_complete, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val messageLabel = view.findViewById<TextView>(R.id.session_complete_message)

        messageLabel.text = message

        if (isError) {
            val icon = view.findViewById<ImageView>(R.id.session_complete_icon)
            icon.setImageResource(R.drawable.ic_error)
        }

        Handler(Looper.getMainLooper()).postDelayed(
            {
                EventBus.broadcast(CheckoutEvent.DismissInternal(CheckoutExitReason.EXIT_SUCCESS))
            },
            delay ?: SESSION_COMPLETE_DISMISS_DELAY_DEFAULT
        )
    }

    companion object {

        private fun getSuccessMessage(successType: SuccessType, context: Context): String {
            return when (successType) {
                SuccessType.DEFAULT ->
                    context.getString(R.string.success_text)
                SuccessType.VAULT_TOKENIZATION_SUCCESS ->
                    context.getString(R.string.payment_method_added_message)
                SuccessType.PAYMENT_SUCCESS ->
                    context.getString(R.string.payment_request_completed_successfully)
            }
        }

        private fun getErrorMessage(errorType: ErrorType, context: Context): String {
            return when (errorType) {
                ErrorType.DEFAULT ->
                    context.getString(R.string.error_default)
                ErrorType.VAULT_TOKENIZATION_FAILED ->
                    context.getString(R.string.payment_method_not_added_message)
                ErrorType.PAYMENT_FAILED ->
                    context.getString(R.string.payment_request_unsuccessful)
            }
        }

        fun newInstance(delay: Int, viewType: SessionCompleteViewType): SessionCompleteFragment {
            return SessionCompleteFragment().apply {
                arguments = Bundle().apply {

                    context?.let {
                        when (viewType) {
                            is SessionCompleteViewType.Error -> {
                                putBoolean(SESSION_COMPLETE_IS_ERROR_KEY, true)
                                putString(
                                    SESSION_COMPLETE_MESSAGE_KEY,
                                    getErrorMessage(viewType.errorType, it),
                                )
                            }
                            is SessionCompleteViewType.Success -> {
                                putBoolean(SESSION_COMPLETE_IS_ERROR_KEY, false)
                                putString(
                                    SESSION_COMPLETE_MESSAGE_KEY,
                                    getSuccessMessage(viewType.successType, it),
                                )
                            }
                        }
                    }

                    putLong(SESSION_COMPLETE_DISMISS_DELAY_KEY, delay.toLong())
                }
            }
        }
    }
}
