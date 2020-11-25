package io.primer.android.ui

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.primer.android.R
import io.primer.android.UniversalCheckout
import io.primer.android.logging.Logger

class CheckoutSheetFragment : BottomSheetDialogFragment(),
  CheckoutSheetFragmentPublisher {
  private val log = Logger("checkout-fragment")
  private var listener: CheckoutSheetFragmentListener? = null
  private lateinit var viewModel: PrimerViewModel

  override fun register(listener: CheckoutSheetFragmentListener) {
    this.listener = listener
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    viewModel = ViewModelProvider(this.requireActivity()).get(PrimerViewModel::class.java)

    viewModel.loading.observe(this, {
      // TODO: hide loading spinner + show UX
    })
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    log("onCreateView")
    return inflater.inflate(R.layout.activity_checkout_sheet, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    when (viewModel.uxMode) {
      UniversalCheckout.UXMode.CHECKOUT -> {
        view.findViewById<TextView>(R.id.primer_sheet_title).setText(R.string.prompt_pay)

        viewModel.amount.let {
          if (it == null) {
            view.findViewById<TextView>(R.id.primer_sheet_title_detail).visibility = View.GONE
          } else {
            // TODO: format the amount nicely
            view.findViewById<TextView>(R.id.primer_sheet_title_detail).setText(it.currency + " " + it.value)
          }
        }
      }
      UniversalCheckout.UXMode.ADD_PAYMENT_METHOD -> {
        view.findViewById<TextView>(R.id.primer_sheet_title).setText(R.string.prompt_add_new_card)
        view.findViewById<TextView>(R.id.primer_sheet_title_detail).visibility = View.GONE

      }
    }
  }

  override fun onDismiss(dialog: DialogInterface) {
    super.onDismiss(dialog)
    this.listener?.onDismissed()
  }

  companion object {
    @JvmStatic
    fun newInstance(bundle: Bundle): CheckoutSheetFragment {
      val fragment = CheckoutSheetFragment()
      fragment.arguments = bundle
      return fragment
    }
  }
}