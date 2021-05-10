package io.primer.android.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import io.primer.android.R
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.model.dto.CheckoutExitReason

private const val SUCCESS_FRAGMENT_DISMISS_DELAY_KEY = "SUCCESS_FRAGMENT_DISMISS_DELAY"
private const val SUCCESS_FRAGMENT_DISMISS_DELAY_DEFAULT = 3000L

enum class SuccessType {
    DEFAULT, ADDED_PAYMENT_METHOD
}

class SuccessFragment : Fragment() {

    private var delay: Long? = null

    internal var successType: SuccessType = SuccessType.DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            delay = it.getLong(SUCCESS_FRAGMENT_DISMISS_DELAY_KEY)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_success, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // set message
        val successMessage = view.findViewById<TextView>(R.id.success_message)

        successMessage.text = when (successType) {
            SuccessType.ADDED_PAYMENT_METHOD -> context?.getString(R.string.payment_method_added_message)
            SuccessType.DEFAULT -> "Success!"
        }

        Handler(Looper.getMainLooper()).postDelayed(
            {
                EventBus.broadcast(CheckoutEvent.DismissInternal(CheckoutExitReason.EXIT_SUCCESS))
            },
            delay ?: SUCCESS_FRAGMENT_DISMISS_DELAY_DEFAULT
        )
    }

    companion object {

        fun newInstance(delay: Int, successType: SuccessType): SuccessFragment {
            return SuccessFragment().apply {

                this.successType = successType

                arguments = Bundle().apply {
                    putLong(SUCCESS_FRAGMENT_DISMISS_DELAY_KEY, delay.toLong())
                }
            }
        }
    }
}
