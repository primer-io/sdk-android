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
      val container: ViewGroup = findViewById(R.id.primer_sheet_payment_methods_list)

      items.forEach { pm ->
        val button = pm.createButton(container)

        button.setOnClickListener {
          this.onPaymentMethodSelected(pm.identifier)
        }
      }
    })

    viewModel.amount.observe(this, {
      findViewById<SelectPaymentMethodTitle>(R.id.primer_sheet_title_layout).setAmount(it)
    })


    viewModel.uxMode.observe(this, {
      findViewById<SelectPaymentMethodTitle>(R.id.primer_sheet_title_layout).setUXMode(it)
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

  private fun <T: View> findViewById(id: Int): T {
    return requireView().findViewById(id)
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