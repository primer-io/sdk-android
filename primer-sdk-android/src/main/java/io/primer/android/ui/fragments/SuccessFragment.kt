package io.primer.android.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.primer.android.R
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.model.dto.CheckoutExitReason

private const val SUCCESS_FRAGMENT_DISMISS_DELAY_KEY = "SUCCESS_FRAGMENT_DISMISS_DELAY"
private const val SUCCESS_FRAGMENT_DISMISS_DELAY_DEFAULT = 3000L

class SuccessFragment : Fragment() {

    private var delay: Long? = null

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
        Handler(Looper.getMainLooper()).postDelayed({
            EventBus.broadcast(CheckoutEvent.DismissInternal(CheckoutExitReason.EXIT_SUCCESS))
        }, delay ?: SUCCESS_FRAGMENT_DISMISS_DELAY_DEFAULT)
    }

    companion object {

        fun newInstance(delay: Int): SuccessFragment {
            return SuccessFragment().apply {
                arguments = Bundle().apply {
                    putLong(SUCCESS_FRAGMENT_DISMISS_DELAY_KEY, delay.toLong())
                }
            }
        }
    }
}
