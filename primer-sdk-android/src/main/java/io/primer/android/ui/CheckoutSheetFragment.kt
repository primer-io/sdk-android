package io.primer.android.ui

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.primer.android.R
import io.primer.android.UniversalCheckout
import io.primer.android.logging.Logger
import io.primer.android.payment.CurrencyFormatter
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.viewmodel.PrimerViewModel

class CheckoutSheetFragment : BottomSheetDialogFragment() {
  private val log = Logger("checkout-fragment")
  private val listeners: MutableList<CheckoutSheetListener> = ArrayList()
  private lateinit var viewModel: PrimerViewModel

  interface CheckoutSheetListener {
    fun onPaymentMethodSelected(type: String)
    fun onSheetDismissed()
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    if (activity is CheckoutSheetListener) {
      listeners.add(activity as CheckoutSheetListener)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    viewModel = ViewModelProvider(this.requireActivity()).get(PrimerViewModel::class.java)

    viewModel.loading.observe(this, { loading ->
      // TODO: hide loading spinner + show UX
    })

    viewModel.paymentMethods.observe(this, { items ->
      val view = requireView()
      val container = view.findViewById<ViewGroup>(R.id.primer_sheet_payment_methods_list)
      val factory = PaymentMethodDescriptor.Factory(viewModel)

      items.forEach { pm ->
        val button = pm.createButton(container)

        button.setOnClickListener {
          this.onPaymentMethodSelected(pm.identifier)
        }
      }
    })
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    log("onCreateView")
    return inflater.inflate(R.layout.activity_checkout_sheet, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    when (viewModel.uxMode.value) {
      UniversalCheckout.UXMode.CHECKOUT -> {
        view.findViewById<TextView>(R.id.primer_sheet_title).setText(R.string.prompt_pay)

        viewModel.amount.value.let { amt ->
          val amount = CurrencyFormatter.format(amt)

          if (amount == null) {
            view.findViewById<TextView>(R.id.primer_sheet_title_detail).visibility = View.GONE
          } else {
            // TODO: format the amount nicely
            view.findViewById<TextView>(R.id.primer_sheet_title_detail).setText(amount)
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
    listeners.forEach {
      it.onSheetDismissed()
    }
  }

  private fun onPaymentMethodSelected(type: String) {
    listeners.forEach {
      it.onPaymentMethodSelected(type)
    }
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